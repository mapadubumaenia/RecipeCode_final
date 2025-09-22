
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
            <sec:authorize access="isAuthenticated()">
                <sec:authentication property="principal.nickname"/>님
            </sec:authorize>
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

        <!-- === For You 추천 피드 (동적 로딩) === -->
        <section id="forYouFeed" class="post-list"></section>
        <div style="display:flex; justify-content:center; margin:12px 0;">
            <button id="forYouMoreBtn" class="btn" style="min-width:140px;">더 보기</button>
        </div>

        <!-- 로그인 사용자의 이메일을 JS에 주입 (미로그인 시 공백) -->
        <script>
            (function setUserEmail(){
                window.__USER_EMAIL__ = '';
            })();
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


        <script>
            (function(){
                var $list = document.getElementById('forYouFeed');
                var $btn  = document.getElementById('forYouMoreBtn');

                var USER_EMAIL = (window.__USER_EMAIL__ || '').trim().toLowerCase();
                var pageSize = 20;
                var nextCursor = null;
                var busy = false;

                function buildUrl() {
                    var url;
                    if (USER_EMAIL && USER_EMAIL.length > 0) {
                        url = '/api/feed/personal?userEmail=' + encodeURIComponent(USER_EMAIL);
                    } else {
                        url = '/api/feed/hot?';
                    }

                    // 쿼리 파라미터 정리
                    if (url.indexOf('?') === -1) {
                        url += '?';
                    } else if (url.charAt(url.length - 1) !== '&' && url.charAt(url.length - 1) !== '?') {
                        url += '&';
                    }

                    if (nextCursor) {
                        url += 'after=' + encodeURIComponent(nextCursor) + '&';
                    }
                    url += 'size=' + encodeURIComponent(pageSize);
                    return url;
                }

                function esc(v){
                    var s = (v == null ? '' : String(v));
                    return s.replace(/&/g,'&amp;')
                        .replace(/</g,'&lt;')
                        .replace(/>/g,'&gt;')
                        .replace(/"/g,'&quot;')
                        .replace(/'/g,'&#39;');
                }

                function pickThumb(it){
                    if (it.thumbUrl && typeof it.thumbUrl === 'string' && it.thumbUrl.trim().length > 0){
                        return it.thumbUrl;
                    }
                    var seed = (it.id || 'recipe').toString().slice(0,12).replace(/[^a-zA-Z0-9]/g,'');
                    return 'https://picsum.photos/seed/' + encodeURIComponent(seed || 'rc') + '/1200/800';
                }

                function cardHtml(it){
                    var tagsHtml = '';
                    if (it.tags && it.tags.length) {
                        var parts = [];
                        for (var i=0;i<it.tags.length;i++){
                            parts.push('<span class="tag">#' + esc(it.tags[i]) + '</span>');
                        }
                        tagsHtml = parts.join(' ');
                    }
                    var score = (typeof it.recScore === 'number' && it.recScore > 0) ? (' · score ' + it.recScore) : '';
                    var likes = (typeof it.likes === 'number') ? it.likes : (it.likes || 0);
                    var thumb = pickThumb(it);

                    var html = ''
                        + '<article class="card p-16 post" data-id="' + esc(it.id || '') + '">'
                        +   '<div class="post-head">'
                        +     '<div class="avatar-ss"><img src="" alt=""></div>'
                        +     '<div class="post-info">'
                        +       '<div class="post-id">@' + esc(it.author || '') + '</div>'
                        +       '<div class="muted">' + esc(it.createdAt || '') + '</div>'
                        +     '</div>'
                        +     '<button class="followbtn-sm" data-user-id="' + esc(it.author || '') + '" data-following="false"></button>'
                        +   '</div>'
                        +   '<div class="thumb">'
                        +     '<img src="' + esc(thumb) + '" alt="' + esc(it.title || '') + '">'
                        +   '</div>'
                        +   '<p class="muted">' + esc(it.title || '') + score + '</p>'
                        +   '<p class="muted">' + tagsHtml + '</p>'
                        +   '<div class="post-cta">'
                        +     '<button class="btn-none">❤️ ' + likes + '</button>'
                        +     '<button class="btn-none post-cmt" data-post-id="' + esc(it.id || '') + '">💬</button>'
                        +     '<button class="btn-none">↗ Share</button>'
                        +   '</div>'
                        + '</article>';
                    return html;
                }

                async function loadMore(){
                    if (busy) return;
                    busy = true;
                    if ($btn) {
                        $btn.disabled = true;
                        $btn.textContent = '불러오는 중…';
                    }

                    try{
                        var url = buildUrl();
                        var res = await fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
                        if (!res.ok) throw new Error('HTTP ' + res.status);
                        var data = await res.json();

                        if (data && data.items && data.items.length) {
                            var html = '';
                            for (var i=0;i<data.items.length;i++){
                                html += cardHtml(data.items[i]);
                            }
                            var temp = document.createElement('div');
                            temp.innerHTML = html;
                            while (temp.firstChild) $list.appendChild(temp.firstChild);
                        }
                        nextCursor = (data && data.next) ? data.next : null;
                    }catch(e){
                        console.error(e);
                        alert('추천 피드를 불러오지 못했어요.');
                    }finally{
                        if ($btn) {
                            if (nextCursor) {
                                $btn.textContent = '더 보기';
                                $btn.disabled = false;
                            } else {
                                $btn.textContent = '마지막입니다';
                                $btn.disabled = true;
                            }
                        }
                        busy = false;
                    }
                }

                // 초기 로드 + 버튼 이벤트
                loadMore();
                if ($btn) $btn.addEventListener('click', loadMore);
            })();
        </script>
        <!-- === /For You 추천 피드 === -->

    </section>

    <!-- 사이드바(태블릿/PC에서 오른쪽) -->
    <aside class="sidebar">
        <!-- 하단 버튼:모바일 display:none -->
        <div class="card p-16 stack-btns">
            <a class="btn pc-register text-center"
               href="<c:url value='/auth/login'/>">Login</a>

            <a class="btn text-center" href="newfeed-ver-mypage-wireframe.html">Profile</a>
            <a class="btn primary text-center" href="<c:url value='/recipes/add'/>">Upload Recipe</a>
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
        <a class="tab is-active" href="newfeed-ver-mypage-wireframe.html">Profile</a>
        <a class="tab" href="create-update.html">Upload</a>
    </nav>
</footer>

<script src="notifs.js"></script>
<script src="feed-follow-btn.js"></script>
<script src="feed-cmt.js"></script>
<script src="footer.js"></script>
</body>
</html>
