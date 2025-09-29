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
  <script src="<c:url value='/js/search.js'/>" defer></script>
  <style>
    .media { width: 100%; border-radius: 12px; overflow: hidden; background: #000; position: relative; }
    .media.aspect { aspect-ratio: 16 / 9; }
    .media > iframe, .media > video, .media > img { width: 100%; height: 100%; display: block; object-fit: cover; }
    .light-yt { cursor: pointer; }
    .light-yt:focus { outline: 3px solid #8ac4ff; outline-offset: 2px; }
    .light-yt .play-badge { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); width: 72px; height: 72px; border-radius: 50%; background: rgba(255,255,255,0.8); display: grid; place-items: center; font-size: 28px; line-height: 1; user-select: none; }
    .light-yt:hover .play-badge { background: rgba(255,255,255,0.95); }
    a.post-link.title { text-decoration: none; }
    a.post-link.title:hover { text-decoration: underline; }
    .tags { display:flex; flex-wrap:wrap; gap:6px; margin-top:6px; }
    .tag { display:inline-block; padding:2px 8px; border-radius:999px; background:#f2f3f5; font-size:12px; color:#333; line-height:20px; white-space:nowrap; }
  </style>
  <sec:authorize access="isAuthenticated()">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
  </sec:authorize>
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
          <sec:authentication property="principal" var="loginUser"/>
          <a href="${pageContext.request.contextPath}/mypage">${loginUser.nickname}</a>님
        </sec:authorize>
        <!-- 알림 버튼 -->
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
              <button class="btn small ghost" id="markAll">모두 읽음</button>
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
  <section class="main">
    <h2 id="foryou" class="section-title">Results</h2>
    <div id="results"></div>
    <div id="resultsSentinel" style="height:1px"></div>
  </section>

  <aside class="sidebar">
    <div class="card p-16 stack-btns">
      <a class="btn pc-register text-center" href="<c:url value='/auth/login'/>">login</a>
      <button class="btn text-center" type="button" disabled aria-disabled="true" title="준비중">Profile</button>
      <a class="btn primary text-center" href="<c:url value='/recipes/add'/>">Upload Recipe</a>
    </div>

    <!-- 사이드바: Following -->
    <aside id="myfollowing" class="sidebar">
      <h2 class="section-title">Following</h2>

      <!-- 비로그인: 안내 카드 -->
      <sec:authorize access="!isAuthenticated()">
        <div class="card p-16 empty-follow" style="text-align:center; padding:24px;">
          <div style="font-size:32px; line-height:1.2; margin-bottom:8px;">🔒</div>
          <p style="margin:4px 0;"><strong>로그인 후 사용 가능합니다</strong></p>
          <p class="muted" style="margin:8px 0 16px;">관심 있는 셰프를 팔로우하면 새 레시피가 여기 표시됩니다.</p>
          <a class="btn primary" href="<c:url value='/auth/login'/>">Login</a>
        </div>
      </sec:authorize>

      <!-- 로그인: 실제 팔로잉 피드 컨테이너 (JS가 채움) -->
      <sec:authorize access="isAuthenticated()">
        <div id="followContainer" class="follow-feed"></div>
      </sec:authorize>
    </aside>
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

<!-- JSP가 가진 값만 전역으로 전파 -->
<script>
  window.__CTX__='${pageContext.request.contextPath}';
  window.__USER_EMAIL__='';
</script>
<sec:authorize access="isAuthenticated()">
  <script>
    (function(){
      var a = '<sec:authentication property="principal.userEmail"/>' || '';
      var b = '<sec:authentication property="principal.username"/>' || '';
      var v = (a && a.trim().length) ? a : (b && b.trim().length ? b : '');
      if (v) window.__USER_EMAIL__ = v.trim().toLowerCase();
    })();
  </script>
</sec:authorize>
<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<%--알림 js--%>
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="<c:url value='/js/login-to-follow.js'/>" defer></script>
<script src="${pageContext.request.contextPath}/js/mainpage-sidebar.js"></script>
</body>
</html>
