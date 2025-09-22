<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Search</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
</head>
<body>
<header class="container">
  <div class="flex-box">
    <h1 class="page-title">Search</h1>
    <a class="home-btn" href="<c:url value='/'/>">home</a>
    <!-- ▶ 추가: 알림 + 로그아웃 -->
    <div class="header-actions">
      <a class="register" href="register_page.html">👤</a>
      <div class="notif-wrap">
        <button
                id="btnNotif"
                class="notif-btn"
                aria-haspopup="dialog"
                aria-expanded="false"
                aria-controls="notifPanel"
                title="알림"
        >
          🔔
          <span class="notif-dot" aria-hidden="true"></span>
        </button>

        <!-- 드롭다운 패널 -->
        <div
                id="notifPanel"
                class="notif-panel"
                role="dialog"
                aria-label="알림 목록"
        >
          <div class="notif-head">
            <strong>알림</strong>
            <div class="actions">
              <button class="btn small ghost" id="markAll">
                모두 읽음
              </button>
            </div>
          </div>

          <div class="notif-list" id="notifList"><!-- JS 렌더 --></div>

          <div class="notif-foot">
            <button class="btn small ghost" id="closeNotif">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</header>



<!-- 서치바 -->
<nav class="container search-bar">
  <!-- 정렬 (표시는 그대로, 값만 new/hot) -->
  <select id="sortSelect" class="tabs select-box">
    <option value="new" selected>lastes</option>
    <option value="hot">likes</option>
  </select>

  <aside class="search-bar">
    <input
            id="q"
            class="search-input"
            type="search"
            placeholder="Search for recipes… (e.g. Spaghetti, Pancakes, Salad)"
    />
    <button id="btnSearch" class="search-btn" aria-label="검색">🔍</button>
  </aside>
</nav>

<main class="container layout">
  <!-- 메인 컬럼 -->
  <section class="main">
    <!-- 쇼츠 (기존 그대로) -->
    <h2 class="section-title">Trending Shorts</h2>
    <div id="trending" class="trend-grid">
      <!-- 카드 1 -->
      <article class="card p-12 trend-card">
        <div class="thumb badge">
          <img src="https://picsum.photos/seed/pasta/800/500" alt="Spaghetti Aglio e Olio" />
        </div>
        <div><div class="trend-title">Spaghetti Aglio e Olio</div></div>
        <div class="actions">
          <div>
            <button class="btn-none">❤️ Like</button>
            <button class="btn-none post-cmt" data-post-id="pasta_101">💬 12</button>
          </div>
          <button class="followbtn-sm" data-user-id="u_123" data-following="false">Follow</button>
        </div>
      </article>

      <!-- 카드 2 -->
      <article class="card p-12 trend-card">
        <div class="thumb">
          <img src="https://picsum.photos/seed/pancake/800/500" alt="Fluffy Pancakes" />
        </div>
        <div><div class="trend-title">Fluffy Pancakes</div></div>
        <div class="actions">
          <button class="act-btn">❤️ Like</button>
          <button class="act-btn">💬 12</button>
          <button class="act-btn">Fllowing</button>
        </div>
      </article>

      <!-- 카드 3 -->
      <article class="card p-12 trend-card">
        <div class="thumb">
          <img src="https://picsum.photos/seed/salad/800/500" alt="Caprese Salad" />
        </div>
        <div><div class="trend-title">Caprese Salad</div></div>
        <div class="actions">
          <button class="act-btn">❤️ Like</button>
          <button class="act-btn">💬 12</button>
          <button class="act-btn">Fllowing</button>
        </div>
      </article>

      <!-- 카드 4 -->
      <article class="card p-12 trend-card">
        <div class="thumb">
          <img src="https://picsum.photos/seed/risotto/800/500" alt="Mushroom Risotto" />
        </div>
        <div><div class="trend-title">Mushroom Risotto</div></div>
        <div class="actions">
          <button class="act-btn">❤️ Like</button>
          <button class="act-btn">💬 12</button>
          <button class="act-btn">Fllowing</button>
        </div>
      </article>
    </div>

    <!-- 서치결과 -->
    <h2 id="foryou" class="section-title">Results</h2>

    <!-- ▶ 결과가 렌더될 컨테이너 (디자인 유지) -->
    <div id="results"></div>
    <!-- 무한 스크롤 센티널(보이지 않음) -->
    <div id="resultsSentinel" style="height:1px"></div>
  </section>

  <!-- 사이드바(태블릿/PC에서 오른쪽) -->
  <aside class="sidebar">
    <div class="card p-16 stack-btns">
      <!-- 1) 회원가입: GET /auth/register -->
      <a class="btn pc-register text-center"
         href="<c:url value='/auth/login'/>">login</a>

      <!-- 2) 마이페이지: 아직 미구현이므로 비활성 처리 -->
      <button class="btn text-center" type="button" disabled aria-disabled="true" title="준비중">Profile</button>

      <!-- 3) 레시피 등록: GET /recipes/add -->
      <a class="btn primary text-center"
         href="<c:url value='/recipes/add'/>">Upload Recipe</a>
    </div>

    <!-- For you: 맞춤피드 -->
    <div class="followingfeed">
      <h2 class="section-title">For you</h2>
      <section id="following" class="card p-16 feature" style="margin-top: 12px">
        <div class="post-head">
          <div class="avatar-ss"><img src="" alt=""></div>
          <div class="post-info mb-8">
            <div class="post-id">John Do</div>
            <div class="muted">Food Enthusiast</div>
          </div>
          <button class="followbtn-sm is-active" data-user-id="u_987" data-following="true"></button>
        </div>
        <div class="thumb">
          <img src="https://picsum.photos/seed/smoothie/1200/800" alt="Smoothie Bowl photo" />
        </div>
        <p class="muted">Hand-picked favorites from our creators.</p>
      </section>
    </div>
  </aside>
</main>

<div class="to-topbox">
  <button id="backToTop" class="to-top" aria-label="맨 위로">Top</button>
</div>

<!-- 모바일:하단 고정, PC: display:none -->
<footer>
  <nav class="tabs">
    <a class="tab is-active" href="newfeed-ver-mypage-wireframe.html">Profile</a>
    <a class="tab" href="create-update.html">Upload</a>
  </nav>
</footer>

<script src="${pageContext.request.contextPath}/notifs.js"></script>
<script src="${pageContext.request.contextPath}/feed-cmt.js"></script>
<script src="${pageContext.request.contextPath}/feed-follow-btn.js"></script>
<script src="${pageContext.request.contextPath}/footer.js"></script>

<!-- ✅ 통합검색 연동 JS (디자인 변경 없음, 무한 스크롤) -->
<script>
  (function() {
    var ctx = '${pageContext.request.contextPath}';
    var $q = document.getElementById('q');
    var $sort = document.getElementById('sortSelect');
    var $btn = document.getElementById('btnSearch');
    var $list = document.getElementById('results');
    var $sentinel = document.getElementById('resultsSentinel');

    var state = {
      q: '',
      sort: 'new',
      next: null,
      loading: false,
      size: 20
    };

    // URL → 입력값/상태 반영
    function seedFromUrl(){
      var params = new URLSearchParams(window.location.search);
      var qParam = params.get('q') || '';
      var sortParam = params.get('sort') || 'new'; // 기본값 new

      $q.value = qParam;                // '#술'처럼 인코딩된 값이 자동 디코딩되어 들어옴
      $sort.value = sortParam;          // 'new' | 'hot' | (필요하면 'rel' → 'new'로 매핑)
      state.q = qParam.trim();
      state.sort = $sort.value || 'new';
      state.next = null;
    }

    // 입력값/정렬 → 주소창 동기화 (뒤로가기/새로고침시 동일 상태 유지)
    function syncUrl(){
      var params = new URLSearchParams();
      if (state.q) params.set('q', state.q);
      if (state.sort) params.set('sort', state.sort);
      var qs = params.toString();
      var url = ctx + '/search' + (qs ? ('?' + qs) : '');
      history.replaceState(null, '', url);
    }

    // (이하 기존 유틸 함수는 그대로)
    function isUuid36(s){
      return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(s || '');
    }
    function fmtDate(v) {
      if (!v) return '';
      try {
        var d = new Date(v);
        if (isNaN(d.getTime())) return '';
        var y = d.getFullYear();
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var day = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + day;
      } catch (e) { return ''; }
    }

    function escapeHtml(s) {
      if (s == null) return '';
      return String(s)
              .replace(/&/g,'&amp;')
              .replace(/</g,'&lt;')
              .replace(/>/g,'&gt;')
              .replace(/"/g,'&quot;')
              .replace(/'/g,'&#39;');
    }

    function renderItem(it){
      try {
        var thumb = (it.thumbUrl && it.thumbUrl.length > 0)
                ? it.thumbUrl
                : 'https://via.placeholder.com/1200x800?text=';

        var title = escapeHtml(it.title || '');
        var nick  = escapeHtml(it.authorNick || '');
        var date  = fmtDate(it.createdAt);
        var likes = (it.likes != null) ? it.likes : 0;
        var cmts  = (it.comments != null) ? it.comments : 0;
        var views = (it.views != null) ? it.views : 0;

        // 상세 링크 (UUID 가드)
        var idOk = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(it.id || '');
        var detailHref = idOk ? (ctx + '/recipes/' + encodeURIComponent(it.id)) : '#';

        var el = document.createElement('article');
        el.className = 'card p-16 post';
        el.innerHTML =
                '<div class="post-head">' +
                '<div class="avatar-ss"><img src="" alt=""></div>' +
                '<div class="post-info">' +
                '<div class="post-id">@' + nick + '</div>' +
                '<div class="muted">' + (date || '') + '</div>' +
                '</div>' +
                '<button class="followbtn-sm" data-user-id="" data-following="false">Follow</button>' +
                '</div>' +
                (idOk ? ('<a class="post-link" href="' + detailHref + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                '<div class="thumb"><img src="' + thumb + '" alt=""></div>' +
                '<p class="muted">' + title + '</p>' +
                (idOk ? '</a>' : '</div>') +
                '<div class="post-cta">' +
                '<button class="btn-none">❤️ ' + likes + '</button>' +
                '<button class="btn-none">💬 ' + cmts + '</button>' +
                '<button class="btn-none" title="views">👁 ' + views + '</button>' +
                '</div>';

        if (!idOk) {
          el.querySelector('.post-link.disabled')?.addEventListener('click', function(e){ e.preventDefault(); });
          console.warn('[search] invalid id:', it.id, 'title=', it.title);
        }

        $list.appendChild(el);
      } catch (e) {
        console.error('[renderItem] failed with item:', it, e);
      }
    }

    async function fetchOnce(initial) {
      if (state.loading) return;
      state.loading = true;

      var url = ctx + '/api/search?q=' + encodeURIComponent(state.q) +
              '&sort=' + encodeURIComponent(state.sort) +
              '&size=' + state.size;
      if (!initial && state.next) {
        url += '&after=' + encodeURIComponent(state.next);
      }

      try {
        var res = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!res.ok) { state.loading = false; return; }
        var data = await res.json();

        if (initial) $list.innerHTML = '';
        (data.items || []).forEach(renderItem);
        state.next = data.next || null;
      } finally {
        state.loading = false;
      }
    }

    function startSearch() {
      state.q = ($q.value || '').trim();
      state.sort = $sort.value || 'new';
      state.next = null;
      syncUrl();           // ★ 주소창 업데이트
      fetchOnce(true);
    }

    // 이벤트
    $btn.addEventListener('click', startSearch);
    $q.addEventListener('keydown', function(e){ if (e.key === 'Enter') startSearch(); });
    $sort.addEventListener('change', startSearch);

    // 무한 스크롤 (그대로)
    var io = new IntersectionObserver(function(entries){
      entries.forEach(function(entry){
        if (entry.isIntersecting && state.next) fetchOnce(false);
      });
    });
    io.observe($sentinel);

    // ★ 초기 진입: URL 파라미터로 먼저 시딩 → 그 다음 최초 검색 실행
    seedFromUrl();
    startSearch();
  })();
</script>
</body>
</html>
