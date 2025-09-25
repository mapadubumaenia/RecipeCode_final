<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Search</title>
  <link rel="preconnect" href="https://www.youtube.com">
  <link rel="preconnect" href="https://i.ytimg.com">
  <link rel="preconnect" href="https://www.google.com">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search.css">
  <style>
    /* ▶ 미디어 공통 스타일 */
    .media {
      width: 100%;
      border-radius: 12px;
      overflow: hidden;
      background: #000;
      position: relative;
    }
    .media.aspect { aspect-ratio: 16 / 9; }
    .media > iframe,
    .media > video,
    .media > img {
      width: 100%;
      height: 100%;
      display: block;
      object-fit: cover;
    }

    /* ▶ 라이트 유튜브: 플레이스홀더 버튼 */
    .light-yt { cursor: pointer; }
    .light-yt:focus { outline: 3px solid #8ac4ff; outline-offset: 2px; }
    .light-yt .play-badge {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);
      width: 72px; height: 72px;
      border-radius: 50%;
      background: rgba(255,255,255,0.8);
      display: grid; place-items: center;
      font-size: 28px; line-height: 1;
      user-select: none;
    }
    .light-yt:hover .play-badge { background: rgba(255,255,255,0.95); }

    /* 제목만 링크 */
    a.post-link.title { text-decoration: none; }
    a.post-link.title:hover { text-decoration: underline; }

    /* ▶ 태그 스타일 (#chips) */
    .tags { display:flex; flex-wrap:wrap; gap:6px; margin-top:6px; }
    .tag {
      display:inline-block;
      padding:2px 8px;
      border-radius:999px;
      background:#f2f3f5;
      font-size:12px;
      color:#333;
      line-height:20px;
      white-space:nowrap;
    }
  </style>
</head>
<body>
<header class="container">
  <div class="flex-box">
    <h1 class="page-title">Search</h1>
    <a class="home-btn" href="<c:url value='/'/>">home</a>
    <div class="header-actions">
      <a class="register" href="register_page.html">👤</a>
      <div class="notif-wrap">
        <sec:authorize access="isAuthenticated()">
          <sec:authentication property="principal.nickname"/>님
        </sec:authorize>
        <button id="btnNotif" class="notif-btn" aria-haspopup="dialog" aria-expanded="false" aria-controls="notifPanel" title="알림">
          🔔
          <span class="notif-dot" aria-hidden="true"></span>
        </button>
        <div id="notifPanel" class="notif-panel" role="dialog" aria-label="알림 목록">
          <div class="notif-head">
            <strong>알림</strong>
            <div class="actions">
              <button class="btn small ghost" id="markAll">모두 읽음</button>
            </div>
          </div>
          <div class="notif-list" id="notifList"></div>
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
  <select id="sortSelect" class="tabs select-box">
    <option value="new" selected>lastes</option>
    <option value="hot">likes</option>
  </select>

  <aside class="search-bar">
    <input id="q" class="search-input" type="search"
           placeholder="Search for recipes… (e.g. Spaghetti, Pancakes, Salad)"/>
    <button id="btnSearch" class="search-btn" aria-label="검색">🔍</button>
  </aside>
</nav>

<main class="container layout">
  <!-- 메인 컬럼 -->
  <section class="main">
    <h2 class="section-title">Trending Shorts</h2>
    <div id="trending" class="trend-grid"></div>

    <h2 id="foryou" class="section-title">Results</h2>
    <div id="results"></div>
    <div id="resultsSentinel" style="height:1px"></div>
  </section>

  <!-- 사이드바 -->
  <aside class="sidebar">
    <div class="card p-16 stack-btns">
      <a class="btn pc-register text-center" href="<c:url value='/auth/login'/>">login</a>
      <button class="btn text-center" type="button" disabled aria-disabled="true" title="준비중">Profile</button>
      <a class="btn primary text-center" href="<c:url value='/recipes/add'/>">Upload Recipe</a>
    </div>

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

<!-- ✅ 통합검색(무한 스크롤) + 라이트 유튜브 + 태그 표시 -->
<script>
  (function() {
    var ctx = '${pageContext.request.contextPath}';
    var $q = document.getElementById('q');
    var $sort = document.getElementById('sortSelect');
    var $btn = document.getElementById('btnSearch');
    var $list = document.getElementById('results');
    var $sentinel = document.getElementById('resultsSentinel');
    var $trending = document.getElementById('trending');

    var state = { q: '', sort: 'new', next: null, loading: false, size: 20 };




    // URL → 상태
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

    // 상태 → URL
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
              .replace(/&/g,'&amp;').replace(/</g,'&lt;')
              .replace(/>/g,'&gt;').replace(/"/g,'&quot;')
              .replace(/'/g,'&#39;');
    }

    // ▶ 라이트 유튜브: 클릭 시에만 iframe 주입
    function attachLightYouTube(container){
      if (!container) return;
      var src = container.getAttribute('data-yt-src');
      if (!src) return;
      var iframe = document.createElement('iframe');
      var finalSrc = src + (src.includes('?') ? '&' : '?') + 'autoplay=1&mute=0';
      iframe.src = finalSrc;
      iframe.title = 'YouTube video player';
      iframe.setAttribute('allow',
              'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share');
      iframe.setAttribute('allowfullscreen', '');
      iframe.setAttribute('loading', 'eager');
      iframe.frameBorder = '0';
      container.innerHTML = '';
      container.appendChild(iframe);
      container.classList.remove('light-yt');
      container.removeAttribute('role');
      container.removeAttribute('tabindex');
      container.removeAttribute('aria-label');
    }

    // 위임 이벤트: 클릭/Enter/Space로 재생
    document.addEventListener('click', function(e){
      var el = e.target.closest('.light-yt[data-yt-src]');
      if (el) attachLightYouTube(el);
    });
    document.addEventListener('keydown', function(e){
      if (e.key !== 'Enter' && e.key !== ' ') return;
      var el = document.activeElement;
      if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
        e.preventDefault();
        attachLightYouTube(el);
      }
    });

    // ▶ 아이템 렌더 (영상/유튜브 라이트/이미지 + 태그)
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

        // --- 미디어 블록 ---
        var kind = it.mediaKind || 'image';
        var mediaHtml = '';
        if (kind === 'youtube') {
          var poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
          var src = it.mediaSrc || '';
          mediaHtml =
                  '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                  'aria-label="' + title + ' 동영상 재생" data-yt-src="' + src + '">' +
                  '<img src="' + poster + '" alt="">' +
                  '<div class="play-badge">▶</div>' +
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

        // --- 태그 렌더 ---
        var tagsHtml = '';
        if (Array.isArray(it.tags) && it.tags.length > 0) {
          var chips = [];
          for (var i = 0; i < it.tags.length; i++) {
            var t = it.tags[i];
            if (t == null) continue;
            var txt = escapeHtml(String(t));
            if (txt.length === 0) continue;
            chips.push('<span class="tag">#' + txt + '</span>');
          }
          if (chips.length) tagsHtml = '<div class="tags">' + chips.join('') + '</div>';
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
                tagsHtml +  /* ← 여기! 태그 출력 */
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

    // ▶ 트렌딩 렌더 (라이트 유튜브 동일 적용)
    function renderTrendingItem(it){
      var wrap = document.createElement('article');
      wrap.className = 'card p-12 trend-card';

      var kind = it.mediaKind || 'image';
      var title = escapeHtml(it.title || '');
      var mediaHtml = '';
      if (kind === 'youtube') {
        var poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
        var src = it.mediaSrc || '';
        mediaHtml =
                '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                'aria-label="' + title + ' 동영상 재생" data-yt-src="' + src + '">' +
                '<img src="' + poster + '" alt="">' +
                '<div class="play-badge">▶</div>' +
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

    function renderEmpty(q) {
      var msg = q
              ? '“' + escapeHtml(q) + '” 에 대한 결과가 없어요.'
              : '검색어를 입력해 보세요.';
      $list.innerHTML =
              '<div class="empty">' +
              '<div class="emoji">🔎</div>' +
              '<p><strong>검색 내용이 없습니다.</strong></p>' +
              '<p class="hint">' + msg + '</p>' +
              '</div>';
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
      if (!initial && state.next) url += '&after=' + encodeURIComponent(state.next);

      try {
        var res = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!res.ok) { state.loading = false; return; }
        var data = await res.json();

        if (initial) $list.innerHTML = '';
        (data.items || []).forEach(renderItem);
        state.next = data.next || null;
        // ✅ 결과 비었을 때 Empty 렌더
        if (initial && (!data.items || data.items.length === 0)) {
          state.next = null;           // 무한스크롤 막기
          renderEmpty(state.q);
        }
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

    // 초기 로드
    seedFromUrl();
    fetchTrending();
    startSearch();
  })();
</script>
</body>
</html>
