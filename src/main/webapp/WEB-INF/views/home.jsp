<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <title>Lumeat — 빛나는 한 끼</title>

    <link rel="preconnect" href="https://www.youtube.com">
    <link rel="preconnect" href="https://i.ytimg.com">
    <link rel="preconnect" href="https://www.google.com">

    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/newfeed-ver-main-wireframe.css'/>">

    <!-- ✅ 전역 값을 가장 먼저 주입 -->
    <script>
        window.__CTX__ = '${pageContext.request.contextPath}';
        window.__USER_EMAIL__ = '';
    </script>
    <sec:authorize access="isAuthenticated()">
        <script>
            (function () {
                var a = '<sec:authentication property="principal.userEmail"/>' || '';
                var b = '<sec:authentication property="principal.username"/>' || '';
                var v = (a && a.trim().length) ? a : (b && b.trim().length ? b : '');
                if (v) window.__USER_EMAIL__ = v.trim().toLowerCase();
            })();
        </script>
    </sec:authorize>

    <!-- ✅ 전역 주입 이후에 JS 로드 (캐시버스터 포함) -->
    <script src="<c:url value='/js/home-popular-tags.js'/>?v=7" defer></script>
    <script src="<c:url value='/js/home.js'/>?v=7" defer></script>

    <style>
        .post-link { display:block; text-decoration:none; color:inherit; }
        .post-link.disabled { cursor:default; }
        .media { width:100%; border-radius:12px; overflow:hidden; background:#000; position:relative; }
        .media.aspect { aspect-ratio:16 / 9; }
        .media > iframe, .media > video, .media > img { width:100%; height:100%; display:block; object-fit:cover; }
        .light-yt { cursor:pointer; }
        .light-yt:focus { outline:3px solid #8ac4ff; outline-offset:2px; }
        .light-yt .play-badge {
            position:absolute; left:50%; top:50%; transform:translate(-50%,-50%);
            width:72px; height:72px; border-radius:50%; background:rgba(255,255,255,.85);
            display:grid; place-items:center; font-size:28px; line-height:1; user-select:none;
        }
        .light-yt:hover .play-badge { background:rgba(255,255,255,.95); }
    </style>

    <sec:authorize access="isAuthenticated()">
        <meta name="_csrf" content="${_csrf.token}"/>
        <meta name="_csrf_header" content="${_csrf.headerName}"/>
    </sec:authorize>
</head>
<body>
<header class="container">
    <div class="flex-box">
        <h1 class="page-title">Lumeat</h1>
        <div class="notif-wrap">
            <!-- ▼ 비로그인일 때만 노출되는 로그인 버튼 -->
            <sec:authorize access="!isAuthenticated()">
                <a class="btn small ghost login-top" href="<c:url value='/auth/login'/>">Login</a>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <sec:authentication property="principal" var="loginUser"/>
                <a class="alink"  href="${pageContext.request.contextPath}/mypage">${loginUser.nickname}님</a>
            </sec:authorize>

            <button id="btnNotif" class="notif-btn" aria-haspopup="dialog" aria-expanded="false"
                    aria-controls="notifPanel" title="알림">🔔
                <span class="notif-dot" aria-hidden="true"></span>
            </button>

            <div id="notifPanel" class="notif-panel" role="dialog" aria-label="알림 목록">
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
</header>

<div class="container search-bar">
    <form action="${pageContext.request.contextPath}/search" method="get">
        <input name="q" class="search-input" type="search"
               placeholder="Search for recipes… (e.g. Spaghetti, Pancakes, Salad)"/>
        <button class="search-btn" aria-label="검색">search</button>
    </form>
</div>

<main class="container layout">
    <section class="main">
        <nav class="tabs" aria-label="Trending tabs">
            <a href="#trending" class="tab is-active">Trending</a>
            <a href="#popular" class="tab">Popular</a>
            <a href="#foryou" class="tab">Foryou</a>
            <a href="#following" class="tab">Following</a>
        </nav>

        <h2 class="section-title">Trending Recipes</h2>
        <div id="trending" class="trend-grid">
            <article class="card p-12 trend-card">
                <div class="thumb badge">
                    <img src="https://picsum.photos/seed/pasta/800/500" alt="Spaghetti Aglio e Olio"/>
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
            <article class="card p-12 trend-card">
                <div class="thumb"><img src="https://picsum.photos/seed/pancake/800/500" alt="Fluffy Pancakes"/></div>
                <div><div class="trend-title">Fluffy Pancakes</div></div>
                <div class="actions">
                    <button class="btn-none">❤️ Like</button>
                    <button class="btn-none">💬 12</button>
                    <button class="btn-none">Follow</button>
                </div>
            </article>
            <article class="card p-12 trend-card">
                <div class="thumb"><img src="https://picsum.photos/seed/salad/800/500" alt="Caprese Salad"/></div>
                <div><div class="trend-title">Caprese Salad</div></div>
                <div class="actions">
                    <button class="btn-none">❤️ Like</button>
                    <button class="btn-none">💬 12</button>
                    <button class="btn-none">Follow</button>
                </div>
            </article>
            <article class="card p-12 trend-card">
                <div class="thumb"><img src="https://picsum.photos/seed/risotto/800/500" alt="Mushroom Risotto"/></div>
                <div><div class="trend-title">Mushroom Risotto</div></div>
                <div class="actions">
                    <button class="btn-none">❤️ Like</button>
                    <button class="btn-none">💬 12</button>
                    <button class="btn-none">Follow</button>
                </div>
            </article>
        </div>

        <section id="popular" class="card populartag p-16">
            <h3 class="section-title">Popular Tags</h3>
            <div class="tags" id="popularTagsWrap">
                <div class="tag-item"><span>🥦 Vegetarian</span><span class="chip">12k</span></div>
                <div class="tag-item"><span>🥩 Meat Lovers</span><span class="chip">8.4k</span></div>
                <div class="tag-item"><span>🥗 Healthy</span><span class="chip">15k</span></div>
                <div class="tag-item"><span>🍰 Desserts</span><span class="chip">9.2k</span></div>
            </div>
        </section>

        <h2 id="foryou" class="section-title">For you</h2>
        <section id="forYouFeed" class="post-list"></section>
        <div id="forYouSentinel" style="height:1px"></div>

        <div style="display:flex; justify-content:center; margin:12px 0;">
            <button id="forYouMoreBtn" class="btn" style="min-width:140px;">더 보기</button>
        </div>
    </section>

    <!-- ✅ 바디 하단 전역 재정의 블록은 삭제됨 -->

    <!-- 사이드바 -->
    <aside class="sidebar">
        <div class="card p-16 stack-btns">
            <a class="btn pc-register text-center" href="<c:url value='/auth/login'/>">Login</a>
            <a class="btn text-center" href="<c:url value='/mypage'/>">Profile</a>
            <a class="btn primary text-center" href="<c:url value='/recipes/add'/>">Upload Recipe</a>
        </div>

        <aside id="myfollowing" class="sidebar">
            <h2 class="section-title">Following</h2>
            <sec:authorize access="!isAuthenticated()">
                <div class="card p-16 empty-follow" style="text-align:center; padding:24px;">
                    <div style="font-size:32px; line-height:1.2; margin-bottom:8px;"><i data-lucide="Lock"></i>
                    </div>
                    <p style="margin:4px 0;"><strong>로그인 후 사용 가능합니다</strong></p>
                    <p class="muted" style="margin:8px 0 16px;">관심 있는 셰프를 팔로우하면 새 레시피가 여기 표시됩니다.</p>
                    <a class="btn primary" href="<c:url value='/auth/login'/>">Login</a>
                </div>
            </sec:authorize>
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
    <div class="authbar">
        <input class="search" type="search" placeholder="재료·요리·해시태그 검색"/>
        <button class="search-btn" aria-label="검색">search</button>
    </div>
    <nav class="tabs">
        <a class="btab is-active" href="/mypage">Profile</a>
        <a class="btab" href="/recipes/add">Upload</a>
    </nav>
</footer>

<!-- 기타 스크립트 -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="feed-follow-btn.js"></script>
<script src="feed-cmt.js"></script>
<script src="footer.js"></script>
<script src="${pageContext.request.contextPath}/js/mainpage-sidebar.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/utils.js"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
<script src="<c:url value='/js/login-to-follow.js'/>" defer></script>
<script src="https://unpkg.com/lucide@latest/dist/umd/lucide.min.js"></script>
<script src="/js/icons-init.js" defer></script>


</body>
</html>
