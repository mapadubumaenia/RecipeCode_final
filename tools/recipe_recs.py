# recipe_recs.py
# 간단 협업필터링(KNN, cosine)으로 개인화 추천을 만들어 ES 인덱스(user-recs-v1)에 저장
# 개선점:
# - Deprecation 제거: es.options(...).search / helpers.bulk(es.options(...), ...)
# - ES 연결 헬스체크(require_es)
# - 대용량 안전 수집: PIT + search_after 페이징
# - 결과 인덱스 매핑 자동 생성(nested items)
# - 이웃 유사도(1 - dist) 가중치 반영
# - 콜드스타트 fallback(인기 아이템)
# - bulk 통계 로그(stats_only)

import os
from datetime import datetime, timezone

import pandas as pd
from sklearn.neighbors import NearestNeighbors
from elasticsearch import Elasticsearch, helpers

# ===== 설정 =====
ES_URL  = os.getenv("ES_URL", "http://192.168.30.34:9200")
ES_USER = os.getenv("ES_USER")  # 필요 시 설정
ES_PASS = os.getenv("ES_PASS")

EVENT_INDEX = os.getenv("EVENT_INDEX", "rc-events-*")   # 입력 이벤트 인덱스 패턴
OUT_INDEX   = os.getenv("OUT_INDEX",   "user-recs-v1")  # 추천 결과 저장 인덱스
TOP_N       = int(os.getenv("TOP_N", "200"))            # 저장할 추천 아이템 수
PAGE_SIZE   = int(os.getenv("PAGE_SIZE", "2000"))       # 이벤트 페이징 크기
K_NEIGHBORS = int(os.getenv("K_NEIGHBORS", "10"))       # 유사 이웃 수

# 이벤트 타입 가중치 (좋아요 > 저장 > 조회)
WEIGHTS = {"like": 3.0, "save": 2.0, "view": 0.5}


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
        raise SystemExit(f"[FATAL] ES 연결 실패: {e}")


# ===== 입력 이벤트 로딩 (PIT + search_after) =====
def iter_events(es: Elasticsearch, index: str, page_size: int = 2000):
    # userId/recipeId/type만 가져온다
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
                "_source": ["userId", "recipeId", "type"],
                "query": {"terms": {"type": ["like", "save", "view"]}},
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
        uid, rid, typ = s.get("userId"), s.get("recipeId"), s.get("type")
        if uid and rid and typ:
            rows.append({"userId": uid, "recipeId": rid, "type": typ})
    df = pd.DataFrame(rows)
    if df.empty:
        print("[INFO] No events found. Exit.")
        raise SystemExit
    return df


# ===== 전처리/모델 =====
def build_matrix(df: pd.DataFrame) -> pd.DataFrame:
    """이벤트 가중치 적용 후 user×item 피벗 매트릭스 생성"""
    df = df.copy()
    df["w"] = df["type"].map(WEIGHTS).fillna(0.0)
    mx = df.pivot_table(
        index="userId",
        columns="recipeId",
        values="w",
        aggfunc="sum",
        fill_value=0.0,
    )
    return mx

def train_model(mx: pd.DataFrame) -> NearestNeighbors:
    """코사인 유사도 기반 KNN 모델 학습"""
    model = NearestNeighbors(metric="cosine", algorithm="brute")
    model.fit(mx.values)
    return model


# ===== 인기 아이템 (콜드스타트/백업용) =====
def top_items(df: pd.DataFrame, n: int = 50):
    tmp = df.copy()
    tmp["w"] = tmp["type"].map(WEIGHTS).fillna(0.0)
    s = tmp.groupby("recipeId")["w"].sum().sort_values(ascending=False)
    return [{"recipeId": rid, "score": float(sc)} for rid, sc in s.head(n).items()]


# ===== 추천 =====
def recommend_for(user_id: str, df: pd.DataFrame, mx: pd.DataFrame,
                  model: NearestNeighbors, k_neighbors: int = 10, top_n: int = 50,
                  fallback_popular=None):
    """한 사용자에 대한 추천 리스트 생성 (이웃 유사도 가중 합산)"""
    if user_id not in mx.index:
        # 완전 신규 유저 → 인기 아이템 반환
        return fallback_popular[:top_n] if fallback_popular else []

    uidx = mx.index.get_loc(user_id)
    vec = mx.to_numpy()[uidx].reshape(1, -1)

    # 이웃 사용자 (자기자신 포함 반환되므로 제외)
    k = min(max(2, k_neighbors), len(mx.index))
    dist, nn_idx = model.kneighbors(vec, n_neighbors=k)
    pairs = [(i, 1.0 - float(d)) for i, d in zip(nn_idx.flatten(), dist.flatten()) if i != uidx]
    pairs = [(i, s) if s > 0 else (i, 0.0) for i, s in pairs]
    pairs = [(i, s) for i, s in pairs if s > 0.0]

    if not pairs:
        return fallback_popular[:top_n] if fallback_popular else []

    neighbor_users = [mx.index[i] for i, _ in pairs]
    sim_map = {mx.index[i]: s for i, s in pairs}

    my_items = set(df.loc[df["userId"] == user_id, "recipeId"].tolist())
    neigh_df = df[(df["userId"].isin(neighbor_users)) & (~df["recipeId"].isin(my_items))].copy()
    if neigh_df.empty:
        return fallback_popular[:top_n] if fallback_popular else []

    neigh_df["w"] = neigh_df["type"].map(WEIGHTS).fillna(0.0)
    neigh_df["sim"] = neigh_df["userId"].map(sim_map).fillna(0.0)
    neigh_df["score"] = neigh_df["w"] * neigh_df["sim"]

    score_series = neigh_df.groupby("recipeId")["score"].sum().sort_values(ascending=False)
    return [{"recipeId": rid, "score": float(sc)} for rid, sc in score_series.head(top_n).items()]


# ===== 결과 인덱스 보장(nested 매핑) =====
def ensure_out_index(es: Elasticsearch, index: str):
    try:
        if es.indices.exists(index=index):
            return
        body = {
            "settings": {"number_of_shards": 1, "number_of_replicas": 0},
            "mappings": {
                "properties": {
                    "userId": {"type": "keyword"},
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
        print(f"[OK] Created index `{index}` with nested mapping.")
    except Exception as e:
        raise SystemExit(f"[FATAL] 결과 인덱스 생성 실패: {e}")


# ===== 저장 =====
def save_all(es: Elasticsearch, df: pd.DataFrame, mx: pd.DataFrame,
             model: NearestNeighbors, out_index: str, top_n: int):
    """모든 사용자 추천을 한 번에 bulk 저장"""
    ts = datetime.now(timezone.utc).isoformat()
    popular = top_items(df, n=top_n)

    actions = []
    for uid in mx.index:
        items = recommend_for(uid, df, mx, model,
                              k_neighbors=K_NEIGHBORS, top_n=top_n,
                              fallback_popular=popular)
        actions.append({
            "_op_type": "index",
            "_index": out_index,
            "_id": uid,  # 문서ID = userId 고정
            "_source": {
                "userId": uid,
                "items": items,           # [{recipeId, score}, ...]
                "updatedAt": ts,
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

    mx = build_matrix(df)
    print(f"[INFO] users: {mx.shape[0]}, items: {mx.shape[1]}")

    model = train_model(mx)
    save_all(es, df, mx, model, OUT_INDEX, TOP_N)


if __name__ == "__main__":
    main()
