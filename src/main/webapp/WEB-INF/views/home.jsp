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
            <div class="tags">
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

        <!-- ✅ 공통 유틸: 컨텍스트/UUID/상세URL -->
        <script>
            var CTX = '${pageContext.request.contextPath}';
            function isUuid36(s){
                return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(s || '');
            }
            function detailUrl(id){ return CTX + '/recipes/' + encodeURIComponent(id); }
        </script>

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
                    if (url.indexOf('?') === -1) url += '?'; else if (!/[&?]$/.test(url)) url += '&';
                    if (nextCursor) url += 'after=' + encodeURIComponent(nextCursor) + '&';
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

                // ▶ 라이트 유튜브/비디오/이미지 블록 생성
                function renderMediaHtml(it){
                    var kind = it.mediaKind || 'image';
                    if (kind === 'youtube') {
                        var poster = it.poster || pickThumb(it);
                        var src = it.mediaSrc || '';
                        return '' +
                            '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                            'aria-label="' + esc(it.title || '') + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                            '<img src="' + esc(poster) + '" alt="">' +
                            '<div class="play-badge">▶</div>' +
                            '</div>';
                    } else if (kind === 'video') {
                        var vsrc = it.mediaSrc || '';
                        var poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                        return '' +
                            '<div class="media aspect">' +
                            '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                            '</div>';
                    } else {
                        var img = (it.mediaSrc && it.mediaSrc.length > 0) ? it.mediaSrc : pickThumb(it);
                        return '' +
                            '<div class="media aspect">' +
                            '<img src="' + esc(img) + '" alt="">' +
                            '</div>';
                    }
                }

                /* ✅ 여기부터: 링크 포함 카드(상세 이동 지원) */
                function safeId(it){
                    // 서비스가 RecipeCardDto.id로 UUID를 내려줌. 혹시 대비해 uuid/_id도 폴백.
                    return (it.id || it.uuid || it._id || '').toString();
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

                    var rid = safeId(it);
                    var hasUuid = isUuid36(rid);
                    var href = hasUuid ? detailUrl(rid) : '#';

                    var mediaBlock = renderMediaHtml(it);

                    var html = ''
                        + '<article class="card p-16 post" data-id="' + esc(rid) + '">'
                        +   '<div class="post-head">'
                        +     '<div class="avatar-ss"><img src="" alt=""></div>'
                        +     '<div class="post-info">'
                        +       '<div class="post-id">@' + esc(it.authorNick || it.author || '') + '</div>'
                        +       '<div class="muted">' + esc(it.createdAt || '') + '</div>'
                        +     '</div>'
                        +     '<button class="followbtn-sm" data-user-id="' + esc(it.authorNick || it.author || '') + '" data-following="false"></button>'
                        +   '</div>'
                        +   mediaBlock
                        +   (hasUuid ? ('<a class="post-link" href="' + href + '">') : '<div class="post-link disabled" aria-disabled="true">')
                        +     '<p class="muted" style="margin-top:8px">' + esc(it.title || '') + score + '</p>'
                        +     (tagsHtml ? ('<p class="muted">' + tagsHtml + '</p>') : '')
                        +   (hasUuid ? '</a>' : '</div>')
                        +   '<div class="post-cta">'
                        +     '<button class="btn-none js-like">❤️ ' + likes + '</button>'
                        +     '<button class="btn-none post-cmt js-cmt" data-post-id="' + esc(rid) + '">💬</button>'
                        +     '<button class="btn-none js-share">↗ Share</button>'
                        +   '</div>'
                        + '</article>';
                    return html;
                }
                /* ✅ 변경 끝 */

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

                /* ✅ 카드 빈공간 클릭 시 상세 이동 (버튼은 이동 막기) */
                document.addEventListener('click', function(e){
                    // 이동 막아야 하는 버튼들
                    if (e.target.closest('.js-like, .js-cmt, .js-share, .followbtn-sm')) {
                        e.stopPropagation();
                        return;
                    }
                    // a.post-link 자체는 기본 동작으로 이동
                    if (e.target.closest('a.post-link')) return;

                    // ▶ 라이트 유튜브 클릭 처리 (iframe 주입)
                    var lyt = e.target.closest('.light-yt[data-yt-src]');
                    if (lyt) { attachLightYouTube(lyt); return; }

                    var card = e.target.closest('article.post[data-id]');
                    if (!card) return;
                    var rid = card.getAttribute('data-id');
                    if (isUuid36(rid)) window.location.href = detailUrl(rid);
                });

                // ▶ 키보드 접근성: Enter/Space 시 라이트 유튜브 재생
                document.addEventListener('keydown', function(e){
                    if (e.key !== 'Enter' && e.key !== ' ') return;
                    var el = document.activeElement;
                    if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
                        e.preventDefault();
                        attachLightYouTube(el);
                    }
                });

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

<script src="notifs.js"></script>
<script src="feed-follow-btn.js"></script>
<script src="feed-cmt.js"></script>
<script src="footer.js"></script>
</body>
</html>
