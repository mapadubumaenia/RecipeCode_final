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
  <style>
    /* ▶ 추가: 미디어 공통 스타일 */
    .media {
      width: 100%;
      border-radius: 12px;
      overflow: hidden;
      background: #000;
    }
    .media.aspect {
      aspect-ratio: 16 / 9;
    }
    .media > iframe,
    .media > video,
    .media > img {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
    }
    /* 제목만 링크인 것을 시각적으로 구분 */
    a.post-link.title { text-decoration: none; }
    a.post-link.title:hover { text-decoration: underline; }
  </style>
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
    <!-- 쇼츠 (예시 카드, 실제 데이터 렌더는 JS가 담당) -->
    <h2 class="section-title">Trending Shorts</h2>
    <div id="trending" class="trend-grid">
      <!-- 서버 데이터로 대체됨 -->
    </div>

    <!-- 서치결과 -->
    <h2 id="foryou" class="section-title">Results</h2>

    <!-- 결과 컨테이너 -->
    <div id="results"></div>
    <!-- 무한 스크롤 센티널 -->
    <div id="resultsSentinel" style="height:1px"></div>
  </section>

  <!-- 사이드바(태블릿/PC에서 오른쪽) -->
  <aside class="sidebar">
    <div class="card p-16 stack-btns">
      <a class="btn pc-register text-center"
         href="<c:url value='/auth/login'/>">login</a>
      <button class="btn text-center" type="button" disabled aria-disabled="true" title="준비중">Profile</button>
      <a class="btn primary text-center"
         href="<c:url value='/recipes/add'/>">Upload Recipe</a>
    </div>

    <!-- For you: 맞춤피드 샘플 -->
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
        <div class="media aspect">
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

<!-- ✅ 통합검색 연동 JS (무한 스크롤 + 동영상/유튜브 즉시 재생 지원) -->
<script>
  (function() {
    var ctx = '${pageContext.request.contextPath}';
    var $q = document.getElementById('q');
    var $sort = document.getElementById('sortSelect');
    var $btn = document.getElementById('btnSearch');
    var $list = document.getElementById('results');
    var $sentinel = document.getElementById('resultsSentinel');
    var $trending = document.getElementById('trending');

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
      var sortParam = params.get('sort') || 'new';

      $q.value = qParam;
      $sort.value = sortParam;
      state.q = qParam.trim();
      state.sort = $sort.value || 'new';
      state.next = null;
    }

    // 주소창 동기화
    function syncUrl(){
      var params = new URLSearchParams();
      if (state.q) params.set('q', state.q);
      if (state.sort) params.set('sort', state.sort);
      var qs = params.toString();
      var url = ctx + '/search' + (qs ? ('?' + qs) : '');
      history.replaceState(null, '', url);
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

    // ▶ 아이템 렌더 (영상/유튜브/이미지 구분해 그리기)
    function renderItem(it){
      try {
        var title = escapeHtml(it.title || '');
        var nick  = escapeHtml(it.authorNick || '');
        var date  = fmtDate(it.createdAt);
        var likes = (it.likes != null) ? it.likes : 0;
        var cmts  = (it.comments != null) ? it.comments : 0;
        var views = (it.views != null) ? it.views : 0;

        var idOk = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(it.id || '');
        var detailHref = idOk ? (ctx + '/recipes/' + encodeURIComponent(it.id)) : '#';

        var kind = it.mediaKind || 'image';
        var mediaHtml = '';
        if (kind === 'youtube') {
          var src = it.mediaSrc || '';
          mediaHtml =
                  '<div class="media aspect">' +
                  '<iframe src="' + src + '" title="' + title + '"' +
                  ' loading="lazy" frameborder="0" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen>' +
                  '</iframe>' +
                  '</div>';
        } else if (kind === 'video') {
          var vsrc = it.mediaSrc || '';
          var poster = it.poster ? (' poster="' + it.poster + '"') : '';
          mediaHtml =
                  '<div class="media aspect">' +
                  '<video controls preload="metadata"' + poster + ' src="' + vsrc + '"></video>' +
                  '</div>';
        } else {
          var img = (it.mediaSrc && it.mediaSrc.length > 0)
                  ? it.mediaSrc
                  : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
          mediaHtml =
                  '<div class="media aspect">' +
                  '<img src="' + img + '" alt="">' +
                  '</div>';
        }

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
                mediaHtml +
                (idOk ? ('<a class="post-link title" href="' + detailHref + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                '<p class="muted" style="margin-top:8px">' + title + '</p>' +
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

    // 트렌딩 섹션 렌더 (동일 로직 재사용)
    function renderTrendingItem(it){
      var wrap = document.createElement('article');
      wrap.className = 'card p-12 trend-card';

      var kind = it.mediaKind || 'image';
      var title = escapeHtml(it.title || '');

      var mediaHtml = '';
      if (kind === 'youtube') {
        var src = it.mediaSrc || '';
        mediaHtml =
                '<div class="media aspect">' +
                '<iframe src="' + src + '" title="' + title + '"' +
                ' loading="lazy" frameborder="0" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen>' +
                '</iframe>' +
                '</div>';
      } else if (kind === 'video') {
        var vsrc = it.mediaSrc || '';
        var poster = it.poster ? (' poster="' + it.poster + '"') : '';
        mediaHtml =
                '<div class="media aspect">' +
                '<video controls preload="metadata"' + poster + ' src="' + vsrc + '"></video>' +
                '</div>';
      } else {
        var img = (it.mediaSrc && it.mediaSrc.length > 0)
                ? it.mediaSrc
                : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
        mediaHtml =
                '<div class="media aspect">' +
                '<img src="' + img + '" alt="">' +
                '</div>';
      }

      wrap.innerHTML =
              mediaHtml +
              '<div><div class="trend-title">' + title + '</div></div>' +
              '<div class="actions">' +
              '<button class="act-btn">❤️ ' + (it.likes || 0) + '</button>' +
              '<button class="act-btn">💬 ' + (it.comments || 0) + '</button>' +
              '</div>';

      $trending.appendChild(wrap);
    }

    async function fetchTrending() {
      try {
        var url = ctx + '/api/trending?size=8';
        var res = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!res.ok) return;
        var data = await res.json();
        $trending.innerHTML = '';
        (data.items || []).forEach(renderTrendingItem);
      } catch (e) {
        console.warn('trending load failed', e);
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
      syncUrl();
      fetchOnce(true);
    }

    // 이벤트
    $btn.addEventListener('click', startSearch);
    $q.addEventListener('keydown', function(e){ if (e.key === 'Enter') startSearch(); });
    $sort.addEventListener('change', startSearch);

    // 무한 스크롤
    var io = new IntersectionObserver(function(entries){
      entries.forEach(function(entry){
        if (entry.isIntersecting && state.next) fetchOnce(false);
      });
    });
    io.observe($sentinel);

    // 초기 시딩 및 로드
    seedFromUrl();
    fetchTrending();  // 트렌딩 먼저
    startSearch();    // 검색 실행
  })();
</script>
</body>
</html>
