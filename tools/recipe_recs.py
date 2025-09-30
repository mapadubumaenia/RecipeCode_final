# recipe_recs.py
# 이메일을 내부 고정 키로 사용하는 간단 협업필터링(KNN, cosine) 추천 배치
# + 팔로우 기반 보강(팔로우한 작성자의 글에 보너스 점수)
# + 모든 공개 레시피에 BASE_PRIOR(기본점수) 시드 → 이벤트가 없던 레시피도 후보 포함
#
# - 입력 이벤트: rc-events-* (userEmail, recipeId, type in {"view","like"})
# - 선택 입력: FOLLOW_INDEX(팔로우 엣지), RECIPE_INDEX(레시피 authorEmail 조회)
# - 출력: user-recs-write (별칭 권장; 실제 인덱스는 user-recs-v2 등)
# - 후보 생성: BASE_PRIOR + KNN 협업필터링 + 팔로우 보너스
# - 콜드스타트: BASE_PRIOR + 인기 아이템 fallback
# - 대용량 수집: PIT + search_after

import os
from datetime import datetime, timezone
from collections import defaultdict

import pandas as pd
from sklearn.neighbors import NearestNeighbors
from elasticsearch import Elasticsearch, helpers

# ===== 설정 =====
ES_URL  = os.getenv("ES_URL", "http://192.168.30.34:9200")
ES_USER = os.getenv("ES_USER")
ES_PASS = os.getenv("ES_PASS")

EVENT_INDEX  = os.getenv("EVENT_INDEX",  "rc-events-*")      # 입력 이벤트 인덱스 패턴
FOLLOW_INDEX = os.getenv("FOLLOW_INDEX", "")                 # 팔로우 인덱스(선택)
RECIPE_INDEX = os.getenv("RECIPE_INDEX", "recipe-v2")        # 레시피 인덱스(id→authorEmail)
OUT_INDEX    = os.getenv("OUT_INDEX",    "user-recs-write")  # 출력(별칭 권장)

TOP_N        = int(os.getenv("TOP_N", "200"))
PAGE_SIZE    = int(os.getenv("PAGE_SIZE", "2000"))
K_NEIGHBORS  = int(os.getenv("K_NEIGHBORS", "10"))

# 이벤트 타입 가중치 (요청: 좋아요는 낮춤)
WEIGHTS = {"view": 0.5, "like": 1.0}

# 팔로우 보너스(요청: 2점)
FOLLOW_BONUS = float(os.getenv("FOLLOW_BONUS", "2.0"))

# 기본 점수(요청: 모든 레시피에 0.1)
BASE_PRIOR = float(os.getenv("BASE_PRIOR", "0.1"))

# ===== ES 연결/헬스체크 =====
def connect_es() -> Elasticsearch:
    kwargs = dict(request_timeout=30, retry_on_timeout=True, max_retries=3)
    if ES_USER and ES_PASS:
        return Elasticsearch(ES_URL, basic_auth=(ES_USER, ES_PASS), verify_certs=False, **kwargs)
    return Elasticsearch(ES_URL, verify_certs=False, **kwargs)

def require_es(es: Elasticsearch):
    try:
        if not es.ping():
            raise RuntimeError("Ping failed")
        info = es.info()
        ver = info.get("version", {}).get("number")
        print(f"[OK] ES {ver} cluster={info.get('cluster_name')}")
    except Exception as e:
        raise SystemExit(f"[FATAL] ES connection failed: {e}")

# ===== 공통 PIT 스트리머 =====
def pit_stream(es: Elasticsearch, index: str, page_size: int, body_builder):
    search = es.options(request_timeout=60).search
    pit = es.open_point_in_time(index=index, keep_alive="1m")["id"]
    sort = [{"_shard_doc": "asc"}]
    search_after = None
    try:
        while True:
            body = body_builder()
            body["size"] = page_size
            body["sort"] = sort
            body["pit"] = {"id": pit, "keep_alive": "1m"}
            if search_after:
                body["search_after"] = search_after
            res = search(body=body)
            hits = res.get("hits", {}).get("hits", [])
            if not hits:
                break
            for h in hits:
                yield h.get("_source", {})
            search_after = hits[-1]["sort"]
    finally:
        try:
            es.close_point_in_time({"id": pit})
        except Exception:
            pass

# ===== 입력 이벤트 로딩 =====
def iter_events(es: Elasticsearch, index: str, page_size: int = 2000):
    def body():
        return {
            "_source": ["userEmail", "recipeId", "type"],
            "query": {"terms": {"type": ["view", "like"]}},
        }
    yield from pit_stream(es, index, page_size, body)

def load_events(es: Elasticsearch) -> pd.DataFrame:
    rows = []
    for s in iter_events(es, EVENT_INDEX, page_size=PAGE_SIZE):
        ue, rid, typ = s.get("userEmail"), s.get("recipeId"), s.get("type")
        if ue and rid and typ:
            rows.append({
                "userEmail": str(ue).strip().lower(),
                "recipeId": str(rid).strip(),
                "type": str(typ).strip().lower()
            })
    df = pd.DataFrame(rows)
    if df.empty:
        print("[INFO] No events found. Exit.")
        raise SystemExit
    return df

# ===== 팔로우 엣지 로딩(선택) =====
def load_follows(es: Elasticsearch):
    """
    ES에 동기화된 팔로우 인덱스가 있을 때 사용.
    문서 예시(둘 중 아무거나 허용):
      { followerEmail, followingEmail }
      { follower_id,  following_id  }  # RDB 필드명 그대로 싱크한 경우
    반환: dict[followerEmail] -> set(followingEmail)
    """
    if not FOLLOW_INDEX:
        print("[INFO] FOLLOW_INDEX not set. Skip follow boost.")
        return {}

    def body():
        return {
            "_source": ["followerEmail", "followingEmail", "follower_id", "following_id"],
            "query": {"match_all": {}}
        }

    edges = defaultdict(set)
    try:
        for s in pit_stream(es, FOLLOW_INDEX, PAGE_SIZE, body):
            fr = s.get("followerEmail") or s.get("follower_id")
            to = s.get("followingEmail") or s.get("following_id")
            if fr and to:
                fr = str(fr).strip().lower()
                to = str(to).strip().lower()
                edges[fr].add(to)
    except Exception as e:
        print(f"[WARN] load_follows failed: {e}")
        return {}
    print(f"[INFO] follows: users={len(edges)}")
    return edges

# ===== 레시피 → 작성자 매핑 =====
def load_recipe_authors(es: Elasticsearch):
    """
    recipe 인덱스에서 {id/uuid, authorEmail, visibility, deleted} 로딩.
    공개(PUBLIC) & 미삭제만 후보에 포함.
    반환:
      rid_to_author: dict[recipeId] -> authorEmail
      author_to_rids: dict[authorEmail] -> list[recipeId]
    """
    rid_to_author = {}
    author_to_rids = defaultdict(list)

    if not RECIPE_INDEX:
        return rid_to_author, author_to_rids

    def body():
        return {
            "_source": ["id", "uuid", "authorEmail", "visibility", "deleted"],
            "query": {
                "bool": {
                    "filter": [
                        {"term": {"visibility": "PUBLIC"}},
                        {"bool": {"must_not": {"term": {"deleted": True}}}}
                    ]
                }
            }
        }

    try:
        for s in pit_stream(es, RECIPE_INDEX, PAGE_SIZE, body):
            rid = s.get("id") or s.get("uuid")
            ae  = s.get("authorEmail")
            if not rid or not ae:
                continue
            rid = str(rid).strip()
            ae  = str(ae).strip().lower()
            rid_to_author[rid] = ae
            author_to_rids[ae].append(rid)
    except Exception as e:
        print(f"[WARN] load_recipe_authors failed: {e}")

    print(f"[INFO] recipe-authors: items={len(rid_to_author)}, authors={len(author_to_rids)}")
    return rid_to_author, author_to_rids

# ===== 전처리/모델 =====
def build_matrix(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["w"] = df["type"].map(WEIGHTS).fillna(0.0)
    mx = df.pivot_table(
        index="userEmail",
        columns="recipeId",
        values="w",
        aggfunc="sum",
        fill_value=0.0,
    )
    return mx

def train_model(mx: pd.DataFrame) -> NearestNeighbors:
    model = NearestNeighbors(metric="cosine", algorithm="brute")
    model.fit(mx.values)
    return model

# ===== 인기 아이템 (콜드스타트) =====
def top_items(df: pd.DataFrame, n: int = 50):
    tmp = df.copy()
    tmp["w"] = tmp["type"].map(WEIGHTS).fillna(0.0)
    s = tmp.groupby("recipeId")["w"].sum().sort_values(ascending=False)
    return [{"recipeId": rid, "score": float(sc)} for rid, sc in s.head(n).items()]

# ===== 추천 =====
def recommend_for(user_email: str,
                  df: pd.DataFrame,
                  mx: pd.DataFrame,
                  model: NearestNeighbors,
                  k_neighbors: int,
                  top_n: int,
                  fallback_popular=None,
                  follows_map=None,
                  rid_to_author=None,
                  author_to_rids=None):
    """
    한 사용자에 대한 추천 리스트 생성:
      0) BASE_PRIOR: 모든 공개 레시피에 기본 점수 시드(본인 글 제외)
      1) 사용자 기반 CF (cosine KNN)
      2) 팔로우한 작성자의 아이템에 FOLLOW_BONUS 가산
    """
    follows_map   = follows_map or {}
    rid_to_author = rid_to_author or {}
    author_to_rids = author_to_rids or {}

    score_map = {}

    # --- 0) BASE_PRIOR: 전체 공개 레시피를 후보에 시드 (본인 글 제외) ---
    if rid_to_author:
        for rid, ae in rid_to_author.items():
            if ae == user_email:
                continue  # 본인 글 제외
            score_map[rid] = BASE_PRIOR

            # 0.5) 내 이벤트 직접 가산  ← 추가
        my_df = df[df["userEmail"] == user_email].copy()
        if not my_df.empty:
            my_df["w"] = my_df["type"].map(WEIGHTS).fillna(0.0)
            for rid, sc in my_df.groupby("recipeId")["w"].sum().items():
                # 내 글은 여전히 제외
                if rid_to_author.get(rid) == user_email:
                    continue
                score_map[rid] = score_map.get(rid, BASE_PRIOR) + float(sc)


    # --- 1) CF 기반 ---
    if user_email in mx.index:
        uidx = mx.index.get_loc(user_email)
        vec = mx.to_numpy()[uidx].reshape(1, -1)

        k = min(max(2, k_neighbors), len(mx.index))
        dist, nn_idx = model.kneighbors(vec, n_neighbors=k)
        pairs = [(i, 1.0 - float(d)) for i, d in zip(nn_idx.flatten(), dist.flatten()) if i != uidx]
        pairs = [(i, s) for i, s in pairs if s > 0.0]

        if pairs:
            neighbor_users = [mx.index[i] for i, _ in pairs]
            sim_map = {mx.index[i]: s for i, s in pairs}

            neigh_df = df[df["userEmail"].isin(neighbor_users)].copy()
            if not neigh_df.empty:
                neigh_df["w"] = neigh_df["type"].map(WEIGHTS).fillna(0.0)
                neigh_df["sim"] = neigh_df["userEmail"].map(sim_map).fillna(0.0)
                neigh_df["score"] = neigh_df["w"] * neigh_df["sim"]
                for rid, sc in neigh_df.groupby("recipeId")["score"].sum().items():
                    # 본인 글은 제외
                    if rid_to_author.get(rid) == user_email:
                        continue
                    score_map[rid] = score_map.get(rid, BASE_PRIOR) + float(sc)

    # --- 2) Follow 보강 ---
    followed = follows_map.get(user_email, set())
    if followed and author_to_rids:
        for ae in followed:
            rids = author_to_rids.get(ae, [])
            for rid in rids:
                # 본인 글 제외
                if rid_to_author.get(rid) == user_email:
                    continue
                score_map[rid] = score_map.get(rid, BASE_PRIOR) + FOLLOW_BONUS



    # 상위 N 정렬
    out = sorted(score_map.items(), key=lambda x: x[1], reverse=True)[:top_n]
    return [{"recipeId": rid, "score": float(sc)} for rid, sc in out]

# ===== 출력 인덱스 보장 =====
def ensure_out_index(es: Elasticsearch, index: str):
    try:
        if es.indices.exists(index=index):
            return
        body = {
            "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0,
                "analysis": {
                    "normalizer": {
                        "lowercase_norm": {"type": "custom", "filter": ["lowercase"]}
                    }
                }
            },
            "mappings": {
                "properties": {
                    "userEmail": {"type": "keyword", "normalizer": "lowercase_norm"},
                    "updatedAt": {"type": "date"},
                    "items": {
                        "type": "nested",
                        "properties": {
                            "recipeId": {"type": "keyword"},
                            "score": {"type": "float"}
                        }
                    }
                }
            }
        }
        es.indices.create(index=index, body=body)
        print(f"[OK] Created index `{index}`")
    except Exception as e:
        print(f"[WARN] ensure_out_index: {e} (skip if alias is already set)")

# ===== 저장 =====
def save_all(es: Elasticsearch,
             df: pd.DataFrame,
             mx: pd.DataFrame,
             model: NearestNeighbors,
             out_index: str,
             top_n: int,
             follows_map,
             rid_to_author,
             author_to_rids):
    ts = datetime.now(timezone.utc).isoformat()
    popular = top_items(df, n=top_n)

    # BASE_PRIOR를 쓰므로 all_users는 “이벤트가 없는 유저”는 없지만,
    # 팔로우만 있는 유저도 포함되도록 합집합 사용
    all_users = set(mx.index) | set(follows_map.keys())

    actions = []
    for ue in sorted(all_users):
        items = recommend_for(
            ue, df, mx, model,
            k_neighbors=K_NEIGHBORS, top_n=top_n,
            fallback_popular=popular,
            follows_map=follows_map,
            rid_to_author=rid_to_author,
            author_to_rids=author_to_rids
        )
        actions.append({
            "_op_type": "index",
            "_index": out_index,
            "_id": ue,
            "_source": {
                "userEmail": ue,
                "items": items,
                "updatedAt": ts
            },
        })

    if not actions:
        print("[WARN] No actions to index.")
        return

    ok, fail = helpers.bulk(es.options(request_timeout=120), actions, stats_only=True)
    print(f"[OK] bulk ok={ok}, fail={fail}")
    print(f"[OK] Indexed recommendations for {len(actions)} users into `{out_index}`")

# ===== main =====
def main():
    es = connect_es()
    print(f"[INFO] ES: {ES_URL}, in={EVENT_INDEX}, out={OUT_INDEX}")
    require_es(es)

    ensure_out_index(es, OUT_INDEX)

    df = load_events(es)
    print(f"[INFO] events: {len(df)}")

    follows_map = load_follows(es)  # 없으면 {}
    rid_to_author, author_to_rids = load_recipe_authors(es)  # 공개/미삭제만

    mx = build_matrix(df)
    print(f"[INFO] users: {mx.shape[0]}, items(by events): {mx.shape[1]}, recipes(public): {len(rid_to_author)}")

    model = train_model(mx)
    save_all(es, df, mx, model, OUT_INDEX, TOP_N, follows_map, rid_to_author, author_to_rids)

if __name__ == "__main__":
    main()
