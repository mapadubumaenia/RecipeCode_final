<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 12.
  Time: 오전 11:59
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>쉐프리드 — 레시피 피드</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/newfeed-ver-main-wireframe.css'/>">
</head>
<body>
<header class="container">
    <div class="flex-box">
        <h1 class="page-title">Recipe Code</h1>
            <a class="register" href="/auth/register">👤</a>
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
            <!-- 카드 1 -->
            <article class="card p-12 trend-card">
                <div class="thumb badge">
                    <!-- 썸네일 이미지 예시: 실제 이미지 쓰려면 src 교체 -->
                    <img
                            src="https://picsum.photos/seed/pasta/800/500"
                            alt="Spaghetti Aglio e Olio"
                    />
                </div>
                <div>
                    <div class="trend-title">Spaghetti Aglio e Olio</div>
                </div>
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
                    <img
                            src="https://picsum.photos/seed/pancake/800/500"
                            alt="Fluffy Pancakes"
                    />
                </div>
                <div>
                    <div class="trend-title">Fluffy Pancakes</div>
                </div>
                <div class="actions">
                    <button class="btn-none">❤️ Like</button>
                    <button class="btn-none">💬 12</button>
                    <button class="btn-none">Follow</button>
                </div>
            </article>

            <!-- 카드 3 -->
            <article class="card p-12 trend-card">
                <div class="thumb">
                    <img
                            src="https://picsum.photos/seed/salad/800/500"
                            alt="Caprese Salad"
                    />
                </div>
                <div>
                    <div class="trend-title">Caprese Salad</div>
                </div>
                <div class="actions">
                    <button class="btn-none">❤️ Like</button>
                    <button class="btn-none">💬 12</button>
                    <button class="btn-none">Follow</button>
                </div>
            </article>

            <!-- 카드 4 -->
            <article class="card p-12 trend-card">
                <div class="thumb">
                    <img
                            src="https://picsum.photos/seed/risotto/800/500"
                            alt="Mushroom Risotto"
                    />
                </div>
                <div>
                    <div class="trend-title">Mushroom Risotto</div>
                </div>
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
            <div class="tags">
                <div class="tag-item">
                    <span>🥦 Vegetarian</span><span class="chip">12k</span>
                </div>
                <div class="tag-item">
                    <span>🥩 Meat Lovers</span><span class="chip">8.4k</span>
                </div>
                <div class="tag-item">
                    <span>🥗 Healthy</span><span class="chip">15k</span>
                </div>
                <div class="tag-item">
                    <span>🍰 Desserts</span><span class="chip">9.2k</span>
                </div>
            </div>
        </section>

        <!-- Personalized Feed -->
        <h2 id="foryou" class="section-title">For you</h2>

        <article class="card p-16 post">
            <div class="post-head">
                <div class="avatar-ss"><img src="" alt=""></div>
                <div class="post-info">
                    <div class="post-id">@CulinaryExpert</div>
                    <div class="muted">2h • Rome, IT</div>
                </div>
                <button class="followbtn-sm" data-user-id="u_123" data-following="false"></button>
            </div>
            <div class="thumb">
                <img
                        src="https://picsum.photos/seed/ravioli/1200/800"
                        alt="Homemade Ravioli photo"
                />
            </div>
            <p class="muted">Homemade Ravioli</p>
            <div class="post-cta">
                <button class="btn-none">❤️ 128</button>
                <button class="btn-none post-cmt" data-post-id="ravioli_555">💬 23</button>
                <button class="btn-none">↗ Share</button>
            </div>
        </article>

        <article class="card p-16 post">
            <div class="post-head">
                <div class="avatar-ss"><img src="" alt=""></div>
                <div class="post-info">
                    <div class="post-id">@HealthyVibes</div>
                    <div class="muted">1d • Seoul, KR</div>
                </div>
                <button class="followbtn-sm" data-user-id="u_123" data-following="false">Fllowing</button>
            </div>
            <div class="thumb">
                <img
                        src="https://picsum.photos/seed/smoothie/1200/800"
                        alt="Smoothie Bowl photo"
                />
            </div>
            <p class="muted">Starting my day with a healthy smoothie bowl 🥣</p>
            <div class="post-cta">
                <button class="btn-none">❤️ 128</button>
                <button class="btn-none">💬 23</button>
                <button class="btn-none">↗ Share</button>
            </div>
        </article>
    </section>

    <!-- 사이드바(태블릿/PC에서 오른쪽) -->
    <aside class="sidebar">
        <!-- 하단 버튼:모바일 display:none -->
        <div class="card p-16 stack-btns">
            <a class="btn pc-register text-center" href="/auth/register">register</a>
            <a class="btn text-center" href="newfeed-ver-mypage-wireframe.html"
            >Profile</a>
            <a class="btn primary text-center" href="create-update.html">Upload Recipe</a>
        </div>
        <!-- 팔로우 피드: -->
        <div class="followingfeed">
            <h2 class="section-title">Following</h2>
            <section
                    id="following"
                    class="card p-16 feature"
                    style="margin-top: 12px"
            >
                <div class="post-head">
                    <div class="avatar-ss"><img src="" alt=""></div>
                    <div class="post-info mb-8">
                        <div class="post-id">John Do</div>
                        <div class="muted">Food Enthusiast</div>
                    </div>
                    <button class="followbtn-sm is-active" data-user-id="u_987" data-following="true"></button>
                </div>
                <div class="thumb">
                    <img
                            src="https://picsum.photos/seed/smoothie/1200/800"
                            alt="Smoothie Bowl photo"
                    />
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
        <input
                class="search"
                type="search"
                placeholder="재료·요리·해시태그 검색"
        />
        <button class="search-btn" aria-label="검색">🔍</button>
    </div>
    <nav class="tabs">
        <a class="tab is-active" href="newfeed-ver-mypage-wireframe.html"
        >Profile</a
        >
        <a class="tab" href="create-update.html">Upload</a>
    </nav>
</footer>

<script src="notifs.js"></script>
<script src="feed-follow-btn.js"></script>
<script src="feed-cmt.js"></script>
<script src="footer.js"></script>
</body>
</html>
