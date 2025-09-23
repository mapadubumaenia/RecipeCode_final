# recipe_recs.py
# 이메일을 내부 고정 키로 사용하는 간단 협업필터링(KNN, cosine) 추천 배치
# - 입력 이벤트: rc-events-* (userEmail, recipeId, type in {"view","like"})
# - 출력: user-recs-write (별칭 권장; 실제 인덱스는 user-recs-v2 등)
# - 후보 생성: KNN 협업필터링 + 이웃 유사도 가중 합산
# - 콜드스타트: 인기 아이템 fallback
# - 대용량 안전 수집: PIT + search_after
# - 매핑 자동 보장: userEmail keyword(lowercase normalizer), nested items

import os
from datetime import datetime, timezone

import pandas as pd
from sklearn.neighbors import NearestNeighbors
from elasticsearch import Elasticsearch, helpers


# ===== 설정 =====
ES_URL  = os.getenv("ES_URL", "http://192.168.30.34:9200")
ES_USER = os.getenv("ES_USER")
ES_PASS = os.getenv("ES_PASS")

EVENT_INDEX = os.getenv("EVENT_INDEX", "rc-events-*")     # 입력 이벤트 인덱스 패턴
OUT_INDEX   = os.getenv("OUT_INDEX",   "user-recs-write") # 출력(별칭 권장)
TOP_N       = int(os.getenv("TOP_N", "200"))
PAGE_SIZE   = int(os.getenv("PAGE_SIZE", "2000"))
K_NEIGHBORS = int(os.getenv("K_NEIGHBORS", "10"))

# ▶ 이벤트 타입 가중치 (save 제거)
WEIGHTS = {"view": 0.5, "like": 3.0}


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


# ===== 입력 이벤트 로딩 (PIT + search_after) =====
def iter_events(es: Elasticsearch, index: str, page_size: int = 2000):
    """
    ES PIT + search_after로 대용량 이벤트를 안전하게 스트리밍.
    userEmail, recipeId, type(view|like)만 수집.
    """
    search = es.options(request_timeout=60).search
    pit = es.open_point_in_time(index=index, keep_alive="1m")["id"]
    sort = [{"_shard_doc": "asc"}]
    search_after = None
    try:
        while True:
            body = {
                "size": page_size,
                "sort": sort,
                "pit": {"id": pit, "keep_alive": "1m"},
                "_source": ["userEmail", "recipeId", "type"],
                "query": {"terms": {"type": ["view", "like"]}},  # ← save 제외
            }
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


# ===== 전처리/모델 =====
def build_matrix(df: pd.DataFrame) -> pd.DataFrame:
    """
    이벤트 가중치 적용 후 user×item 피벗 매트릭스 생성
    """
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
def recommend_for(user_email: str, df: pd.DataFrame, mx: pd.DataFrame,
                  model: NearestNeighbors, k_neighbors: int = 10, top_n: int = 50,
                  fallback_popular=None):
    """
    한 사용자에 대한 추천 리스트 생성 (이웃 유사도 가중 합산)
    """
    if user_email not in mx.index:
        return fallback_popular[:top_n] if fallback_popular else []

    uidx = mx.index.get_loc(user_email)
    vec = mx.to_numpy()[uidx].reshape(1, -1)

    # 이웃 사용자 (자기자신 제외)
    k = min(max(2, k_neighbors), len(mx.index))
    dist, nn_idx = model.kneighbors(vec, n_neighbors=k)
    pairs = [(i, 1.0 - float(d)) for i, d in zip(nn_idx.flatten(), dist.flatten()) if i != uidx]
    pairs = [(i, s) for i, s in pairs if s > 0.0]
    if not pairs:
        return fallback_popular[:top_n] if fallback_popular else []

    neighbor_users = [mx.index[i] for i, _ in pairs]
    sim_map = {mx.index[i]: s for i, s in pairs}

    my_items = set(df.loc[df["userEmail"] == user_email, "recipeId"].tolist())
    neigh_df = df[(df["userEmail"].isin(neighbor_users)) & (~df["recipeId"].isin(my_items))].copy()
    if neigh_df.empty:
        return fallback_popular[:top_n] if fallback_popular else []

    neigh_df["w"] = neigh_df["type"].map(WEIGHTS).fillna(0.0)
    neigh_df["sim"] = neigh_df["userEmail"].map(sim_map).fillna(0.0)
    neigh_df["score"] = neigh_df["w"] * neigh_df["sim"]

    score_series = neigh_df.groupby("recipeId")["score"].sum().sort_values(ascending=False)
    return [{"recipeId": rid, "score": float(sc)} for rid, sc in score_series.head(top_n).items()]


# ===== 출력 인덱스 보장 =====
def ensure_out_index(es: Elasticsearch, index: str):
    """
    out_index가 '별칭'일 수도 있으니, 존재 확인만 하고 없으면 새 인덱스를 만든다.
    (운영에선 미리 user-recs-v2를 만들고 별칭을 user-recs-write에 붙이는 방식을 권장)
    """
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
        # 별칭에 대해 생성 시도하면 실패할 수 있음 — 운영에선 별칭/백킹인덱스를 미리 준비하는 것을 권장
        print(f"[WARN] ensure_out_index: {e} (skip if alias is already set)")


# ===== 저장 =====
def save_all(es: Elasticsearch, df: pd.DataFrame, mx: pd.DataFrame,
             model: NearestNeighbors, out_index: str, top_n: int):
    ts = datetime.now(timezone.utc).isoformat()
    popular = top_items(df, n=top_n)

    actions = []
    for ue in mx.index:
        items = recommend_for(
            ue, df, mx, model,
            k_neighbors=K_NEIGHBORS, top_n=top_n,
            fallback_popular=popular
        )
        actions.append({
            "_op_type": "index",
            "_index": out_index,     # 별칭 쓰는 걸 권장 (user-recs-write)
            "_id": ue,               # 문서ID = 이메일(소문자)
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

    # 별칭을 OUT_INDEX로 쓰고 있다면, ensure_out_index는 무시되어도 OK
    ensure_out_index(es, OUT_INDEX)

    df = load_events(es)
    print(f"[INFO] events: {len(df)}")

    mx = build_matrix(df)
    print(f"[INFO] users: {mx.shape[0]}, items: {mx.shape[1]}")

    model = train_model(mx)
    save_all(es, df, mx, model, OUT_INDEX, TOP_N)


if __name__ == "__main__":
    main()
