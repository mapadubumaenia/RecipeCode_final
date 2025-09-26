// =========================
// home.js (홈 메인 피드)
// =========================
(function () {
    "use strict";

    // ------- 공통 전역 -------
    const CTX = (typeof window !== "undefined" && window.__CTX__) ? window.__CTX__ : "";
    const USER_EMAIL = (typeof window !== "undefined" && window.__USER_EMAIL__) ? String(window.__USER_EMAIL__).trim().toLowerCase() : "";

    // DOM ready 보장 (defer면 즉시 실행되지만 방어용)
    function ready(fn){
        if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn);
        else fn();
    }

    // HTML escape (XSS 방지)
    function esc(s){
        return (s==null?'':String(s))
            .replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }

    // UUID 판별 & 상세 URL
    function isUuid36(s){
        return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(s || '');
    }
    function detailUrl(id){ return CTX + '/recipes/' + encodeURIComponent(id); }

    // 썸네일 선택 폴백
    function pickThumb(it){
        if (it.thumbUrl && typeof it.thumbUrl === 'string' && it.thumbUrl.trim().length > 0){
            return it.thumbUrl;
        }
        const seed = (it.id || 'recipe').toString().slice(0,12).replace(/[^a-zA-Z0-9]/g,'');
        return 'https://picsum.photos/seed/' + encodeURIComponent(seed || 'rc') + '/1200/800';
    }

    // 라이트 유튜브 attach
    function attachLightYouTube(container){
        if (!container) return;
        const src = container.getAttribute('data-yt-src');
        if (!src) return;
        const iframe = document.createElement('iframe');
        const finalSrc = src + (src.includes('?') ? '&' : '?') + 'autoplay=1&mute=0';
        iframe.src = finalSrc;
        iframe.title = 'YouTube video player';
        iframe.setAttribute('allow','accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share');
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

    // =========================
    //  A) Popular Tags (트렌딩 태그)
    // =========================
    function setupPopularTags(){
        const $wrap = document.getElementById('popularTagsWrap');
        if (!$wrap) return;

        const fmt = new Intl.NumberFormat('en', { notation: 'compact', maximumFractionDigits: 1 });

        async function loadTrendingTags(days=30, size=4){
            const url = CTX + '/api/trends/tags?days=' + encodeURIComponent(days) + '&size=' + encodeURIComponent(size);
            const res = await fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
            if (!res.ok) throw new Error('HTTP '+res.status);
            return res.json(); // { items: [{tag, count}, ...] }
        }

        function render(items){
            if (!Array.isArray(items) || items.length === 0) return; // 비면 기존 하드코딩 유지
            const frag = document.createDocumentFragment();
            items.forEach(it=>{
                const tag = (it && typeof it.tag === 'string') ? it.tag.trim() : '';
                const cnt = (it && typeof it.count === 'number') ? it.count : 0;
                if (!tag) return;

                const node = document.createElement('div');
                node.className = 'tag-item';
                node.innerHTML = '<span>#' + esc(tag) + '</span><span class="chip">' + esc(fmt.format(cnt)) + '</span>';
                frag.appendChild(node);
            });
            if (frag.childNodes.length > 0) {
                $wrap.innerHTML = '';
                $wrap.appendChild(frag);
            }
        }

        loadTrendingTags(30, 4)
            .then(({items}) => render(items))
            .catch(err => console.warn('[PopularTags] load failed:', err));
    }

    // =========================
    //  B) For You Feed (개인화/핫피드)
    // =========================
    function setupForYou(){
        const $list = document.getElementById('forYouFeed');
        const $btn  = document.getElementById('forYouMoreBtn');
        const $sentinel = document.getElementById('forYouSentinel');
        if (!$list || !$btn) return;

        let pageSize = 5;
        let nextCursor = null;
        let busy = false;

        function buildUrl() {
            let url;
            if (USER_EMAIL && USER_EMAIL.length > 0) {
                url = CTX + '/api/feed/personal?userEmail=' + encodeURIComponent(USER_EMAIL);
            } else {
                url = CTX + '/api/feed/hot?';
            }
            if (url.indexOf('?') === -1) url += '?'; else if (!/[&?]$/.test(url)) url += '&';
            if (nextCursor) url += 'after=' + encodeURIComponent(nextCursor) + '&';
            url += 'size=' + encodeURIComponent(pageSize);
            return url;
        }

        function renderMediaHtml(it){
            const kind = it.mediaKind || 'image';
            if (kind === 'youtube') {
                const poster = it.poster || pickThumb(it);
                const src = it.mediaSrc || '';
                return ''
                    + '<div class="media aspect light-yt" role="button" tabindex="0" '
                    + 'aria-label="' + esc(it.title || '') + ' 동영상 재생" data-yt-src="' + esc(src) + '">'
                    +   '<img src="' + esc(poster) + '" alt="">'
                    +   '<div class="play-badge">▶</div>'
                    + '</div>';
            } else if (kind === 'video') {
                const vsrc = it.mediaSrc || '';
                const poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                return ''
                    + '<div class="media aspect">'
                    +   '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>'
                    + '</div>';
            } else {
                const img = (it.mediaSrc && it.mediaSrc.length > 0) ? it.mediaSrc : pickThumb(it);
                return ''
                    + '<div class="media aspect">'
                    +   '<img src="' + esc(img) + '" alt="">'
                    + '</div>';
            }
        }

        function safeId(it){
            return (it.id || it.uuid || it._id || '').toString();
        }

        function cardHtml(it){
            // 작성자 id/nick → 링크용 데이터 준비
            const displayIdRaw = it.authorId || it.authorNick || it.author || '';
            const cleanId = (displayIdRaw.startsWith('@') ? displayIdRaw.slice(1) : displayIdRaw).trim();
            const profileHref = CTX + '/profile/' + encodeURIComponent(cleanId);

            let tagsHtml = '';
            if (it.tags && it.tags.length) {
                const parts = [];
                for (let i=0;i<it.tags.length;i++){
                    parts.push('<span class="tag">#' + esc(it.tags[i]) + '</span>');
                }
                tagsHtml = parts.join(' ');
            }
            const score = (typeof it.recScore === 'number' && it.recScore > 0) ? (' · score ' + it.recScore) : '';
            const likes = (typeof it.likes === 'number') ? it.likes : (it.likes || 0);

            const rid = safeId(it);
            const hasUuid = isUuid36(rid);
            const href = hasUuid ? detailUrl(rid) : '#';

            const mediaBlock = renderMediaHtml(it);

            const html = ''
                + '<article class="card p-16 post" data-id="' + esc(rid) + '">'
                +   '<div class="post-head">'
                +     '<div class="avatar-ss"><img src="" alt=""></div>'
                +     '<div class="post-info">'
                +       '<div class="post-id">'
                +         (cleanId ? '<a class="author-link" href="' + profileHref + '">@' + esc(cleanId) + '</a>' : '')
                +       '</div>'
                +       '<div class="muted">' + esc(it.createdAt || '') + '</div>'
                +     '</div>'
                +     '<button class="followbtn-sm" data-user-id="' + (cleanId ? '@' + esc(cleanId) : '') + '" data-following="false"></button>'
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

        async function loadMore(){
            if (busy) return;
            busy = true;
            if ($btn) {
                $btn.disabled = true;
                $btn.textContent = '불러오는 중…';
            }

            try{
                const url = buildUrl();
                const res = await fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
                if (!res.ok) throw new Error('HTTP ' + res.status);
                const data = await res.json();

                if (data && data.items && data.items.length) {
                    let html = '';
                    for (let i=0;i<data.items.length;i++){
                        html += cardHtml(data.items[i]);
                    }
                    const temp = document.createElement('div');
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

        // 무한 스크롤
        if ('IntersectionObserver' in window && $sentinel) {
            const io = new IntersectionObserver((entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting && nextCursor && !busy) {
                        loadMore();
                    }
                });
            }, { root: null, rootMargin: '600px 0px' });
            io.observe($sentinel);
        } else {
            // Fallback: 스크롤 바닥 근처에서 로드
            let ticking = false;
            window.addEventListener('scroll', () => {
                if (ticking) return;
                ticking = true;
                requestAnimationFrame(() => {
                    const nearBottom = window.innerHeight + window.scrollY >= (document.body.offsetHeight - 600);
                    if (nearBottom && nextCursor && !busy) loadMore();
                    ticking = false;
                });
            });
        }

        // 카드 빈공간 클릭 시 상세 이동 (버튼은 이동 막기) + 라이트 유튜브
        document.addEventListener('click', function(e){
            if (e.target.closest('.js-like, .js-cmt, .js-share, .followbtn-sm, .author-link')) {
                // 버튼/작성자링크 자체는 기본 동작 유지
                return;
            }
            if (e.target.closest('a.post-link')) return;

            const lyt = e.target.closest('.light-yt[data-yt-src]');
            if (lyt) { attachLightYouTube(lyt); return; }

            const card = e.target.closest('article.post[data-id]');
            if (!card) return;
            const rid = card.getAttribute('data-id');
            if (isUuid36(rid)) window.location.href = detailUrl(rid);
        });

        // 키보드 접근성: Enter/Space 시 라이트 유튜브 재생
        document.addEventListener('keydown', function(e){
            if (e.key !== 'Enter' && e.key !== ' ') return;
            const el = document.activeElement;
            if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
                e.preventDefault();
                attachLightYouTube(el);
            }
        });
    }

    // =========================
    //  C) Trending (최근 7일 좋아요 Top-4)
    // =========================
    function setupTrending(){
        const wrap = document.getElementById('trending');
        if (!wrap) return;

        // 카드 1개 렌더 (유튜브/비디오/이미지 + 제목 + ❤️/💬/👁)
        function renderCard(it){
            const title = esc(it.title || '');
            const likes = it.likes ?? 0;
            const cmts  = it.comments ?? 0;
            const views = it.views ?? 0;

            const idOk = isUuid36(it.id || '');
            const href = idOk ? detailUrl(it.id) : '#';

            // 미디어 블록
            let mediaHtml = '';
            const kind = it.mediaKind || 'image';
            if (kind === 'youtube') {
                const poster = it.poster || '';
                const src = it.mediaSrc || '';
                mediaHtml =
                    '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                    'aria-label="' + title + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                    '<img src="' + esc(poster) + '" alt="">' +
                    '<div class="play-badge">▶</div>' +
                    '</div>';
            } else if (kind === 'video') {
                const vsrc = it.mediaSrc || '';
                const poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                mediaHtml =
                    '<div class="media aspect">' +
                    '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                    '</div>';
            } else {
                const img = it.mediaSrc && it.mediaSrc.length ? it.mediaSrc : (it.poster || pickThumb(it));
                mediaHtml =
                    '<div class="media aspect">' +
                    '<img src="' + esc(img) + '" alt="">' +
                    '</div>';
            }

            const el = document.createElement('article');
            el.className = 'card p-12 trend-card';
            el.innerHTML =
                mediaHtml +
                (idOk ? ('<a class="post-link" href="' + href + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                '<div class="trend-title">' + title + '</div>' +
                '<div class="actions">' +
                '<button class="btn-none">❤️ ' + likes + '</button>' +
                '<button class="btn-none">💬 ' + cmts  + '</button>' +
                '<button class="btn-none" title="views">👁 ' + views + '</button>' +
                '</div>' +
                (idOk ? '</a>' : '</div>');

            if (!idOk) {
                el.querySelector('.post-link.disabled')?.addEventListener('click', e => e.preventDefault());
            }
            wrap.appendChild(el);
        }

        // 데이터 로드
        (async function(){
            try {
                const url = CTX + '/api/home/trending?days=7&size=4';
                const res = await fetch(url, { headers: { 'Accept':'application/json' }, credentials: 'same-origin' });
                if (!res.ok) throw new Error('HTTP '+res.status);
                const data = await res.json();
                wrap.innerHTML = ''; // 하드코딩 초기 내용 비움
                (data.items || []).forEach(renderCard);
            } catch (e) {
                console.warn('[home:trending] load failed', e);
            }
        })();
    }

    // 초기화
    ready(function(){
        setupPopularTags();
        setupForYou();
        setupTrending();
    });
})();
