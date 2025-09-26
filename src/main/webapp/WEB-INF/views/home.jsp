<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>쉐프리드 — 레시피 피드</title>
    <link rel="preconnect" href="https://www.youtube.com">
    <link rel="preconnect" href="https://i.ytimg.com">
    <link rel="preconnect" href="https://www.google.com">
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/newfeed-ver-main-wireframe.css'/>">
    <script src="<c:url value='/js/home-popular-tags.js'/>" defer></script>
    <script src="<c:url value='/js/home.js'/>" defer></script>
    <style>
        /* 링크가 카드 영역을 블록으로 덮도록 */
        .post-link { display:block; text-decoration:none; color:inherit; }
        .post-link.disabled { cursor:default; }

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
            left: 50%; top: 50%;
            transform: translate(-50%, -50%);
            width: 72px; height: 72px;
            border-radius: 50%;
            background: rgba(255,255,255,0.85);
            display: grid; place-items: center;
            font-size: 28px; line-height: 1;
            user-select: none;
        }
        .light-yt:hover .play-badge { background: rgba(255,255,255,0.95); }
    </style>
</head>
<body>
<header class="container">
    <div class="flex-box">
        <h1 class="page-title">Recipe Code</h1>
        <a class="register" href="/auth/register">👤</a>
        <div class="notif-wrap">
            <sec:authorize access="isAuthenticated()">
                <sec:authentication property="principal" var="loginUser"/>
                ${loginUser.nickname}님
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
</header>
<div class="container search-bar">
    <form action="${pageContext.request.contextPath}/search" method="get">
        <input
                name="q"
                class="search-input"
                type="search"
                placeholder="Search for recipes… (e.g. Spaghetti, Pancakes, Salad)"
        />
        <button class="search-btn" aria-label="검색">🔍</button>
    </form>
</div>

<main class="container layout">
    <!-- 메인 컬럼 -->
    <section class="main">
        <!-- Trending -->
        <nav class="tabs" aria-label="Trending tabs">
            <a href="#trending" class="tab is-active">Trending</a>
            <a href="#popular" class="tab">Popular</a>
            <a href="#foryou" class="tab">Foryou</a>
            <a href="#following" class="tab">Following</a>
        </nav>

        <h2 class="section-title">Trending Recipes</h2>
        <div id="trending" class="trend-grid">
            <!-- (데모 이미지들 그대로 두어도 됨. 실제로는 서버 데이터로 대체 가능) -->
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

        <!-- 인기 태그 -->
        <section id="popular" class="card populartag p-16">
            <h3 class="section-title">Popular Tags</h3>
            <div class="tags" id="popularTagsWrap">
                <!-- JS가 성공하면 이 영역을 비우고 실제 집계값으로 채움 -->
                <div class="tag-item"><span>🥦 Vegetarian</span><span class="chip">12k</span></div>
                <div class="tag-item"><span>🥩 Meat Lovers</span><span class="chip">8.4k</span></div>
                <div class="tag-item"><span>🥗 Healthy</span><span class="chip">15k</span></div>
                <div class="tag-item"><span>🍰 Desserts</span><span class="chip">9.2k</span></div>
            </div>
        </section>

        <!-- Personalized Feed -->
        <h2 id="foryou" class="section-title">For you</h2>

        <!-- === For You 추천 피드 (동적 로딩) === -->
        <section id="forYouFeed" class="post-list"></section>
        <!-- ✅ 무한스크롤 센티넬 -->
        <div id="forYouSentinel" style="height:1px"></div>

        <div style="display:flex; justify-content:center; margin:12px 0;">
            <button id="forYouMoreBtn" class="btn" style="min-width:140px;">더 보기</button>
        </div>


    </section>

    <!-- ✅ JSP가 가진 값만 전역으로 전파 -->
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

    <!-- 사이드바(태블릿/PC에서 오른쪽) -->
    <aside class="sidebar">
        <!-- 하단 버튼:모바일 display:none -->
        <div class="card p-16 stack-btns">
            <a class="btn pc-register text-center"
               href="<c:url value='/auth/login'/>">Login</a>

            <a class="btn text-center" href="<c:url value='/mypage'/>"
            >Profile</a>
            <!-- 3) 레시피 등록: GET /recipes/add -->
            <a class="btn primary text-center"
               href="<c:url value='/recipes/add'/>">Upload Recipe</a>
        </div>
        <!-- 팔로우 피드: -->
        <div class="followingfeed">
            <h2 class="section-title">Following</h2>
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
                    <img src="https://picsum.photos/seed/smoothie/1200/800" alt="Smoothie Bowl photo"/>
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
    <div class="authbar">
        <input class="search" type="search" placeholder="재료·요리·해시태그 검색"/>
        <button class="search-btn" aria-label="검색">🔍</button>
    </div>
    <nav class="tabs">
        <a class="tab is-active" href="newfeed-ver-mypage-wireframe.html">Profile</a>
        <a class="tab" href="create-update.html">Upload</a>
    </nav>
</footer>

<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="feed-follow-btn.js"></script>
<script src="feed-cmt.js"></script>
<script src="footer.js"></script>
<%--시간 js--%>
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<%--알림 js--%>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
</body>
</html>
