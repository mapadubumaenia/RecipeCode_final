<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
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
      <a class="btn pc-register text-center" href="register_page.html">register</a>
      <a class="btn text-center" href="newfeed-ver-mypage-wireframe.html">Profile</a>
      <a class="btn primary text-center" href="create-update.html">Upload Recipe</a>
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

    function fmtDate(v) {
      if (!v) return '';
      try {
        var d = new Date(v);
        if (isNaN(d.getTime())) return (typeof v === 'string') ? v : '';
        return d.getFullYear() + '-' +
                String(d.getMonth()+1).padStart(2,'0') + '-' +
                String(d.getDate()).padStart(2,'0');
      } catch(e) { return ''; }
    }

    function escapeHtml(s) {
      if (s == null) return '';
      return String(s)
              .replace(/&/g,'&amp;').replace(/</g,'&lt;')
              .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }

    function renderItem(it) {
      var thumb = (it.thumbUrl && it.thumbUrl.length > 0)
              ? it.thumbUrl
              : 'https://via.placeholder.com/1200x800?text=';
      var title = escapeHtml(it.title || '');
      var nick  = escapeHtml(it.authorNick || '');
      var date  = fmtDate(it.createdAt);
      var likes = (it.likes != null) ? it.likes : 0;
      var cmts  = (it.comments != null) ? it.comments : 0;
      var views = (it.views != null) ? it.views : 0;

      // 상세페이지 링크 (컨텍스트패스 + /recipes/{id})
      var detailHref = ctx + '/recipes/' + encodeURIComponent(it.id);

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

              // 🔗 클릭영역: 이미지와 제목을 링크로 감쌈
              '<a class="post-link" href="' + detailHref + '" aria-label="상세 보기: ' + title + '">' +
              '<div class="thumb"><img src="' + thumb + '" alt=""></div>' +
              '<p class="muted">' + title + '</p>' +
              '</a>' +

              '<div class="post-cta">' +
              '<button class="btn-none">❤️ ' + likes + '</button>' +
              '<button class="btn-none">💬 ' + cmts + '</button>' +
              '<button class="btn-none" title="views">👁 ' + views + '</button>' +
              '</div>';

      $list.appendChild(el);
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
        var res = await fetch(url);
        if (!res.ok) { state.loading = false; return; }
        var data = await res.json();
        // 첫 페이지면 초기화
        if (initial) $list.innerHTML = '';

        (data.items || []).forEach(renderItem);
        state.next = data.next || null;
      } catch (e) {
        // noop
      } finally {
        state.loading = false;
      }
    }

    function startSearch() {
      state.q = ($q.value || '').trim();
      state.sort = $sort.value || 'new';
      state.next = null;
      fetchOnce(true);
    }

    // 이벤트
    $btn.addEventListener('click', function() { startSearch(); });
    $q.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') startSearch();
    });
    $sort.addEventListener('change', function() { startSearch(); });

    // 무한 스크롤
    var io = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting && state.next) {
          fetchOnce(false);
        }
      });
    });
    io.observe($sentinel);

    // 초기 로드: 최신(new) 전체
    startSearch();
  })();
</script>
</body>
</html>
