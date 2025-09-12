<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <title>통합검색</title>
  <style>
    body { font-family: system-ui,-apple-system,Segoe UI,Roboto,Apple SD Gothic Neo,Malgun Gothic,sans-serif; margin: 20px; }
    .toolbar { display:flex; gap:8px; align-items:center; margin-bottom:16px; }
    .toolbar input { flex:1; padding:10px; font-size:16px; }
    .toolbar select, .toolbar button { padding:10px; font-size:16px; }
    .item { border:1px solid #eee; border-radius:10px; padding:14px; margin:10px 0; }
    .item h3 { margin:0 0 6px; font-size:18px; }
    .meta { color:#666; font-size:13px; display:flex; gap:12px; }
    #loading { text-align:center; padding:14px; color:#666; display:none; }
    #end { text-align:center; padding:14px; color:#aaa; display:none; }
  </style>
</head>
<body>
<h1>통합검색 데모</h1>

<div class="toolbar">
  <!-- param.q 값이 있으면 안전하게 이스케이프해서 채워줌 -->
  <input id="q" type="text" placeholder="#비건 / @u1234 / 김치찌개"
         value="<c:out value='${param.q}'/>"/>
  <select id="sort">
    <option value="rel" <c:if test="${param.sort == 'rel' || empty param.sort}">selected</c:if>>관련도</option>
    <option value="new" <c:if test="${param.sort == 'new'}">selected</c:if>>최신순</option>
    <option value="hot" <c:if test="${param.sort == 'hot'}">selected</c:if>>인기순</option>
  </select>
  <button id="searchBtn">검색</button>
</div>

<div id="list"></div>
<div id="loading">로딩 중…</div>
<div id="end">더 이상 결과가 없습니다.</div>
<div id="sentinel" style="height:1px;"></div>

<!-- 컨텍스트패스 안전하게 처리 -->
<script>
  const API = '<c:url value="/api/search"/>';
</script>

<script>
  let next = null;
  let loading = false;

  function render(items) {
    const list = document.getElementById('list');
    for (const it of items) {
      // XSS 예방: textContent 사용 (브라우저가 자동 이스케이프)
      const div = document.createElement('div');
      div.className = 'item';

      const h3 = document.createElement('h3');
      h3.textContent = it.title ?? '(제목 없음)';

      const tags = Array.isArray(it.tags) ? it.tags.join(', ') : '';
      const meta = document.createElement('div');
      meta.className = 'meta';
      meta.textContent = '작성자: ' + (it.authorNick ?? '-') +
                                  ' · 좋아요: ' + (it.likes ?? 0) +
                                  ' · 태그: ' + tags;

      div.appendChild(h3);
      div.appendChild(meta);
      list.appendChild(div);
    }
  }

  function setLoading(v) {
    loading = v;
    document.getElementById('loading').style.display = v ? 'block' : 'none';
  }

  async function loadMore(params) {
    if (loading) return;
    setLoading(true);

    const q = new URLSearchParams({ ...params, size: '20' });
    if (next) q.set('after', next);

    const res = await fetch(API + '?' + q.toString(), {
      headers: { 'Accept': 'application/json' }   // JSON 강제 (혹시 모를 406/HTML 방지)
    });
    if (!res.ok) {
      setLoading(false);
      console.error('API error', res.status);
      return;
    }
    const data = await res.json();

    render(data.items || []);
    next = data.next ?? null;
    setLoading(false);

    // 더 이상 없으면 안내 문구 표시
    if (!next) {
      document.getElementById('end').style.display = 'block';
      observer.disconnect();
    }
  }

  function currentParams() {
    const q = document.getElementById('q').value.trim();
    const sort = document.getElementById('sort').value;
    return { q, sort };
  }


  document.getElementById('q').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') document.getElementById('searchBtn').click();
  });

  // 무한 스크롤
  const observer = new IntersectionObserver((entries) => {
    if (entries.some(e => e.isIntersecting)) {
      if (next !== null) loadMore(currentParams());
    }
  });
  observer.observe(document.getElementById('sentinel'));

  // 초기 로딩: param.q가 있으면 그걸로, 없으면 빈 검색(전체)
  (function init() {
    // JSTL로 채운 input 값을 그대로 사용
    next = null;
    loadMore(currentParams());
  })();

//URL 동기화(검색 누를 때 주소창 갱신) 새로고침/공유해도 같은 결과 나오게:
  document.getElementById('searchBtn').addEventListener('click', () => {
    const { q, sort } = currentParams();
    const params = new URLSearchParams({ q, sort });
    history.replaceState(null, '', `${location.pathname}?${params.toString()}`);

    next = null;
    document.getElementById('end').style.display = 'none';
    document.getElementById('list').innerHTML = '';
    loadMore({ q, sort });
  });

  // 정렬 변경시 자동 재검색
  document.getElementById('sort').addEventListener('change', () => {
    document.getElementById('searchBtn').click();
  });

</script>

</body>
</html>
