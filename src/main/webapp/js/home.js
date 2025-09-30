// =========================
// home.js  (Main/Home page)
// =========================
(function () {
    "use strict";

    // ------- 공통 전역 -------
    const CTX = (typeof window !== "undefined" && window.__CTX__) ? window.__CTX__ : "";
    const USER_EMAIL = (typeof window !== "undefined" && window.__USER_EMAIL__) ? String(window.__USER_EMAIL__).trim().toLowerCase() : "";

    // DOM ready 보장
    function ready(fn){ if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn); else fn(); }

    // 유틸
    function esc(s){ return (s==null?'':String(s)).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&quot;').replace(/'/g,'&#39;'); }
    function isUuid36(s){ return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(s || ''); }
    function detailUrl(id){ return CTX + '/recipes/' + encodeURIComponent(id); }

    function pickThumb(it){
        if (it && typeof it.thumbUrl === 'string' && it.thumbUrl.trim()) return it.thumbUrl;
        const seed = (it?.id || it?.uuid || 'recipe').toString().slice(0,12).replace(/[^a-zA-Z0-9]/g,'');
        return 'https://picsum.photos/seed/' + encodeURIComponent(seed || 'rc') + '/1200/800';
    }

    function toInt(v){ const n = Number(v); return Number.isFinite(n) && n >= 0 ? (n|0) : 0; }
    function pickCommentCount(it){
        const keys = ['comments','commentCount','commentsCount','replies','replyCount','cmtCount','cmts','totalComments'];
        for (let i=0;i<keys.length;i++){ if (it && it[keys[i]] != null) return toInt(it[keys[i]]); }
        return 0;
    }

    // ISO 문자열/epoch 숫자 → "YYYY-MM-DD HH:mm" (로컬시간)
    function fmtYmdHm(input){
        if (input == null) return '';
        let d;
        if (typeof input === 'number') {
            // epoch(초) 혹은 ms 구분
            d = new Date(input > 1e12 ? input : input * 1000);
        } else {
            const s = String(input).trim();
            // 숫자 형태면 epoch로 처리
            if (/^\d+$/.test(s)) {
                const n = Number(s);
                d = new Date(n > 1e12 ? n : n * 1000);
            } else {
                // 일반 ISO 포함 어떤 날짜 문자열도 Date가 파싱
                d = new Date(s);
            }
        }
        if (isNaN(d.getTime())) return ''; // 파싱 실패시 빈 문자열

        const y  = d.getFullYear();
        const m  = String(d.getMonth()+1).padStart(2,'0');
        const dd = String(d.getDate()).padStart(2,'0');
        const hh = String(d.getHours()).padStart(2,'0');
        const mm = String(d.getMinutes()).padStart(2,'0');
        return `${y}-${m}-${dd} ${hh}:${mm}`;
    }

    // ===== 좋아요 UI 헬퍼 =====
    function applyLikeVisual(btn, liked){
        btn.dataset.liked = liked ? 'true' : 'false';
        btn.classList.toggle('is-liked', !!liked);
        btn.setAttribute('aria-pressed', liked ? 'true' : 'false');
    }
    function getLikeCount(btn){
        const s = btn.querySelector('.like-count');
        const n = Number(s ? s.textContent : 0);
        return Number.isFinite(n) ? n : 0;
    }
    function setLikeCount(btn, n){
        const s = btn.querySelector('.like-count');
        const v = Math.max(0, n|0);
        if (s) s.textContent = String(v);
        else btn.innerHTML = `❤️ <span class="like-count">${v}</span>`;
    }

    // 댓글 카운트 보정(0인 카드만 서버로 확인)
    async function hydrateCommentCountsIn(container){
        try {
            const targets = Array.from(container.querySelectorAll('.post-cta .js-cmt[data-post-id]'));
            const need = targets.filter(btn=>{
                const span = btn.querySelector('.cmt-count');
                return toInt(span ? span.textContent : 0) === 0;
            });
            await Promise.all(need.map(async (btn)=>{
                const uuid = btn.getAttribute('data-post-id') || '';
                if (!uuid) return;
                try {
                    const res = await fetch(`${CTX}/comments/count/${encodeURIComponent(uuid)}`, {
                        headers: { 'Accept':'application/json' }, credentials:'same-origin'
                    });
                    if (!res.ok) return;
                    const j = await res.json(); // { commentsCount: number }
                    const c = toInt(j.commentsCount ?? j.count ?? j.commentCount);
                    const span = btn.querySelector('.cmt-count');
                    if (span) span.textContent = String(c);
                    else btn.innerHTML = '💬 <span class="cmt-count">'+c+'</span>';
                } catch {}
            }));
        } catch (e) { console.warn('[hydrateCommentCountsIn] skipped:', e); }
    }

    // 내가 누른 좋아요 표시(배치 → 실패 시 조용히 무시)
    async function hydrateMyLikesIn(container){
        try {
            if (!USER_EMAIL) return;
            const btns = Array.from(container.querySelectorAll('.js-like[data-uuid]')).filter(b => !b.hasAttribute('disabled'));
            if (btns.length === 0) return;
            const ids = Array.from(new Set(btns.map(b => b.getAttribute('data-uuid')).filter(Boolean)));
            if (ids.length === 0) return;

            const CHUNK = 50;
            for (let i=0;i<ids.length;i+=CHUNK){
                const chunk = ids.slice(i, i+CHUNK);
                try{
                    // 배치 엔드포인트가 없다면 404일 수 있으므로 try/catch
                    const res = await fetch(CTX + '/api/recipes/likes/mine?ids=' + encodeURIComponent(chunk.join(',')), {
                        headers: { 'Accept':'application/json' }, credentials:'same-origin'
                    });
                    if (!res.ok) throw new Error('batch http '+res.status);
                    const body = await res.json();
                    const map = (body && body.map) || {};
                    chunk.forEach(id=>{
                        const liked = !!map[id];
                        container.querySelectorAll(`.js-like[data-uuid="${id.replace(/"/g,'\\"')}"]`).forEach(btn=>{
                            applyLikeVisual(btn, liked);
                        });
                    });
                }catch{
                    // 폴백: 단건 상태 확인 엔드포인트가 없으면 그냥 skip
                    // (엔드포인트가 있으면 아래를 살리고, 없으면 조용히 통과)
                    // try {
                    //     await Promise.all(chunk.map(async (id)=>{
                    //         const r = await fetch(`${CTX}/api/recipes/${encodeURIComponent(id)}/like/status`, {
                    //             headers:{'Accept':'application/json'}, credentials:'same-origin'
                    //         });
                    //         if (!r.ok) return;
                    //         const b = await r.json();
                    //         const liked = !!b?.liked;
                    //         container.querySelectorAll(`.js-like[data-uuid="${id.replace(/"/g,'\\"')}"]`).forEach(btn=>{
                    //             applyLikeVisual(btn, liked);
                    //         });
                    //     }));
                    // } catch {}
                }
            }
        } catch (e) { console.warn('[hydrateMyLikesIn] skipped:', e); }
    }

    // uuid 안전 추출
    function pickUuid(it){
        const a = (it && typeof it.uuid === 'string') ? it.uuid : '';
        const b = (it && typeof it.id === 'string') ? it.id : '';
        if (isUuid36(a)) return a;
        if (isUuid36(b)) return b;
        return null;
    }

    // @userId → 프로필 이미지 URL
    function profileImgUrlFromAtUserId(atUserId){
        if (!atUserId || !atUserId.trim()) return null;
        return CTX + '/member/' + encodeURIComponent(atUserId) + '/profile-image';
    }

    // 컨테이너 내부 아바타 하이드레이션 (404면 빈 상태 유지)
    function hydrateAvatarsIn(container){
        const imgs = container.querySelectorAll('.avatar-ss img[data-user-id]');
        imgs.forEach(img=>{
            const atId = img.getAttribute('data-user-id');
            if (!atId) return;
            const url = profileImgUrlFromAtUserId(atId);
            if (!url) return;
            img.onerror = function(){ this.removeAttribute('src'); };
            img.src = url;
        });
    }

    // 라이트 유튜브
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

    // ===== CSRF =====
    function readCookie(name){
        const m = document.cookie.match(new RegExp('(?:^|; )' + name.replace(/([$?*|{}\]\\^])/g,'\\$1') + '=([^;]*)'));
        return m ? decodeURIComponent(m[1]) : null;
    }
    function getCsrf() {
        const metaTok = document.querySelector('meta[name="_csrf"]');
        const metaHdr = document.querySelector('meta[name="_csrf_header"]');
        let token = metaTok ? metaTok.getAttribute('content') : null;
        let header = metaHdr ? metaHdr.getAttribute('content') : null;
        if (!token) { const c = readCookie('XSRF-TOKEN'); if (c) { token = c; header = header || 'X-XSRF-TOKEN'; } }
        return { token, header: header || 'X-CSRF-TOKEN' };
    }

    // ===== 팔로잉 세트 로딩/하이드레이션 =====
    let FOLLOWING_SET = null; // Set<"@userId">
    async function loadMyFollowingSetOnce() {
        if (FOLLOWING_SET || !USER_EMAIL) return FOLLOWING_SET || new Set();
        try {
            const res = await fetch(CTX + "/api/follow/mine/following-ids", {
                credentials: "same-origin",
                headers: { "Accept": "application/json" }
            });
            const arr = res.ok ? await res.json() : [];
            FOLLOWING_SET = new Set(Array.isArray(arr) ? arr : []);
        } catch { FOLLOWING_SET = new Set(); }
        return FOLLOWING_SET;
    }
    function applyFollowVisual(btn, isFollowing){
        if (!btn) return;
        btn.dataset.following = isFollowing ? "true" : "false";
        btn.classList.toggle("is-active", !!isFollowing);
        btn.textContent = isFollowing ? "Following" : "Follow";
        btn.disabled = !!isFollowing; // 이 화면에선 언팔 금지
        if (isFollowing) btn.title = "이미 팔로우 중";
    }
    async function hydrateFollowButtonsIn(container){
        const set = await loadMyFollowingSetOnce();
        container.querySelectorAll('.followbtn-sm[data-user-id]').forEach(btn => {
            const uid = btn.getAttribute('data-user-id') || "";
            applyFollowVisual(btn, set.has(uid));
        });
    }

    // =========================
    //  Popular Tags
    // =========================
    function setupPopularTags(){
        try{
            const $wrap = document.getElementById('popularTagsWrap');
            if (!$wrap) return;

            const fmt = new Intl.NumberFormat('en', { notation: 'compact', maximumFractionDigits: 1 });

            async function loadTrendingTags(days=30, size=4){
                const url = CTX + '/api/trends/tags?days=' + encodeURIComponent(days) + '&size=' + encodeURIComponent(size);
                const res = await fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
                if (!res.ok) throw new Error('HTTP '+res.status);
                return res.json();
            }

            function render(items){
                if (!Array.isArray(items) || items.length === 0) return;
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
                if (frag.childNodes.length > 0) { $wrap.innerHTML = ''; $wrap.appendChild(frag); }
            }

            loadTrendingTags(30, 4).then(({items}) => render(items)).catch(err => console.warn('[PopularTags] load failed:', err));
        }catch(e){ console.warn('[setupPopularTags] skipped:', e); }
    }

    // =========================
    //  For You Feed
    // =========================
    function setupForYou(){
        try{
            const $list = document.getElementById('forYouFeed');
            const $btn  = document.getElementById('forYouMoreBtn');
            const $sentinel = document.getElementById('forYouSentinel');
            if (!$list || !$btn) return;

            let pageSize = 5;
            let nextCursor = null;
            let busy = false;

            function buildUrl() {
                let url;
                if (USER_EMAIL) url = CTX + '/api/feed/personal?userEmail=' + encodeURIComponent(USER_EMAIL);
                else url = CTX + '/api/feed/hot?';
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
                    return '' + '<div class="media aspect light-yt" role="button" tabindex="0" '
                        + 'aria-label="' + esc(it.title || '') + ' 동영상 재생" data-yt-src="' + esc(src) + '">'
                        +   '<img src="' + esc(poster) + '" alt="">'
                        +   '<div class="play-badge">▶</div>'
                        + '</div>';
                } else if (kind === 'video') {
                    const vsrc = it.mediaSrc || '';
                    const poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                    return '' + '<div class="media aspect">'
                        +   '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>'
                        + '</div>';
                } else {
                    const img = (it.mediaSrc && it.mediaSrc.length > 0) ? it.mediaSrc : pickThumb(it);
                    return '' + '<div class="media aspect">'
                        +   '<img src="' + esc(img) + '" alt="">'
                        + '</div>';
                }
            }

            function cardHtml(it){
                const displayIdRaw = it.authorId || it.authorNick || it.author || '';
                const cleanId = (displayIdRaw.startsWith('@') ? displayIdRaw.slice(1) : displayIdRaw).trim();
                const userIdAttr = cleanId ? ('@' + cleanId) : '';
                const profileHref = CTX + '/profile/' + encodeURIComponent(cleanId);
                const authorEmail = (it.authorEmail || '').trim().toLowerCase();

                let tagsHtml = '';
                if (it.tags && it.tags.length) {
                    const parts = [];
                    for (let i=0;i<it.tags.length;i++){ parts.push('<span class="tag">#' + esc(it.tags[i]) + '</span>'); }
                    tagsHtml = parts.join(' ');
                }
                const score = (typeof it.recScore === 'number' && it.recScore > 0) ? (' · score ' + it.recScore) : '';
                const likes = (typeof it.likes === 'number') ? it.likes : (it.likes || 0);
                const cmts  = pickCommentCount(it);

                const uuid = pickUuid(it);
                const hasUuid = !!uuid;
                const href = hasUuid ? detailUrl(uuid) : '#';
                const mediaBlock = renderMediaHtml(it);
                const self = (USER_EMAIL && authorEmail && USER_EMAIL === authorEmail);

                return '' +
                    '<article class="card p-16 post" data-id="' + esc(uuid || '') + '">' +
                    '  <div class="post-head">' +
                    '    <div class="avatar-ss"><img src="" alt="" data-user-id="' + esc(userIdAttr) + '"></div>' +
                    '    <div class="post-info">' +
                    '      <div class="post-id">' + (cleanId ? '<a class="author-link" href="' + profileHref + '">@' + esc(cleanId) + '</a>' : '') + '</div>' +
                    '      <div class="muted"><time datetime="' + esc(it.createdAt || '') + '">' + esc(fmtYmdHm(it.createdAt)) + '</time></div>' +
                    '    </div>' +
                    '    <button class="followbtn-sm' + (self ? ' is-self' : '') + '"' +
                    '       data-user-id="' + esc(userIdAttr) + '"' +
                    '       data-user-email="' + esc(authorEmail) + '"' +
                    '       data-following="false"' +
                    (self ? ' disabled' : '') + '>' +
                    (self ? 'Me' : 'Follow') +
                    '    </button>' +
                    '  </div>' +
                    mediaBlock +
                    (hasUuid ? ('<a class="post-link" href="' + href + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                    '  <p class="muted" style="margin-top:8px">' + esc(it.title || '') + score + '</p>' +
                    (tagsHtml ? ('<p class="muted">' + tagsHtml + '</p>') : '') +
                    (hasUuid ? '</a>' : '</div>') +
                    '  <div class="post-cta">' +
                    '    <button class="btn-none js-like" ' +
                    (hasUuid && !self
                        ? ('data-uuid="' + esc(uuid) + '" ')
                        : 'disabled aria-disabled="true" title="' + (self ? '내 게시물은 좋아요를 누를 수 없어요' : '이 카드엔 uuid가 없어요') + '" ') +
                    '            data-liked="false" aria-pressed="false">' +
                    '      ❤️ <span class="like-count">' + likes + '</span>' +
                    '    </button>' +
                    '    <button class="btn-none post-cmt js-cmt" data-post-id="' + esc(uuid || '') + '">💬 <span class="cmt-count">' + cmts + '</span></button>' +
                    '    <button class="btn-none js-share">↗ Share</button>' +
                    '  </div>' +
                    '</article>';
            }

            async function loadMore(){
                if (busy) return;
                busy = true;
                if ($btn) { $btn.disabled = true; $btn.textContent = '불러오는 중…'; }

                try{
                    const url = buildUrl();
                    const res = await fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    const data = await res.json();

                    if (data && data.items && data.items.length) {
                        let html = '';
                        for (let i=0;i<data.items.length;i++){ html += cardHtml(data.items[i]); }
                        const temp = document.createElement('div');
                        temp.innerHTML = html;
                        while (temp.firstChild) $list.appendChild(temp.firstChild);

                        // 렌더 후 하이드레이션
                        await hydrateFollowButtonsIn($list);
                        hydrateAvatarsIn($list);
                        await hydrateCommentCountsIn($list);
                        await hydrateMyLikesIn($list);   // 내 좋아요 표시
                    }
                    nextCursor = (data && data.next) ? data.next : null;
                }catch(e){
                    console.error(e);
                    alert('추천 피드를 불러오지 못했어요.');
                }finally{
                    if ($btn) {
                        if (nextCursor) { $btn.textContent = '더 보기'; $btn.disabled = false; }
                        else { $btn.textContent = '마지막입니다'; $btn.disabled = true; }
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
                    entries.forEach((entry) => { if (entry.isIntersecting && nextCursor && !busy) { loadMore(); } });
                }, { root: null, rootMargin: '600px 0px' });
                io.observe($sentinel);
            } else {
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

            // 카드 클릭/유튜브
            document.addEventListener('click', function(e){
                if (e.target.closest('.js-like, .js-cmt, .js-share, .followbtn-sm, .author-link')) { return; }
                if (e.target.closest('a.post-link')) return;
                const lyt = e.target.closest('.light-yt[data-yt-src]');
                if (lyt) { attachLightYouTube(lyt); return; }
                const card = e.target.closest('article.post[data-id]');
                if (!card) return;
                const rid = card.getAttribute('data-id');
                if (isUuid36(rid)) window.location.href = detailUrl(rid);
            });
            document.addEventListener('keydown', function(e){
                if (e.key !== 'Enter' && e.key !== ' ') return;
                const el = document.activeElement;
                if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
                    e.preventDefault();
                    attachLightYouTube(el);
                }
            });
        }catch(e){ console.warn('[setupForYou] skipped:', e); }
    }

    // =========================
    //  Trending
    // =========================
    function setupTrending(){
        try{
            const wrap = document.getElementById('trending');
            if (!wrap) return;

            function renderCard(it){
                const title = esc(it.title || '');
                const likes = it.likes ?? 0;
                const cmts  = it.comments ?? 0;
                const views = it.views ?? 0;

                const uuid = pickUuid(it);
                const idOk = !!uuid;
                const href = idOk ? detailUrl(uuid) : '#';

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
                    (idOk ? '</a>' : '</div>') +
                    '<div class="actions">' +
                    '<button class="btn-none js-like" ' +
                    (idOk ? ('data-uuid="' + esc(uuid) + '" ') : 'disabled aria-disabled="true" title="이 카드엔 uuid가 없어요" ') +
                    'data-liked="false" aria-pressed="false">' +
                    '❤️ <span class="like-count">' + likes + '</span>' +
                    '</button>' +

                    '<button class="btn-none" title="views">👁 ' + views + '</button>' +
                    '</div>';

                if (!idOk) { el.querySelector('.post-link.disabled')?.addEventListener('click', e => e.preventDefault()); }
                wrap.appendChild(el);
            }

            (async function(){
                try {
                    const url = CTX + '/api/home/trending?days=7&size=4';
                    const res = await fetch(url, { headers: { 'Accept':'application/json' }, credentials: 'same-origin' });
                    if (!res.ok) throw new Error('HTTP '+res.status);
                    const data = await res.json();
                    wrap.innerHTML = '';
                    (data.items || []).forEach(renderCard);

                    // 트렌딩도 내 좋아요 표시(있으면)
                    await hydrateMyLikesIn(wrap);
                } catch (e) {
                    console.warn('[home:trending] load failed', e);
                }
            })();
        }catch(e){ console.warn('[setupTrending] skipped:', e); }
    }

    // =========================
    //  좋아요 토글 (메인 공통, 낙관적 UI)
    // =========================
    document.addEventListener('click', function(e){
        const btn = e.target.closest('.js-like');
        if (!btn) return;

        if (btn.hasAttribute('disabled')) return;
        const uuid = btn.getAttribute('data-uuid');
        if (!uuid) return;

        if (!USER_EMAIL) { location.href = CTX + '/auth/login'; return; }

        if (btn.dataset.pending === '1') return;
        btn.dataset.pending = '1';

        const prevLiked = (btn.dataset.liked === 'true');
        const prevCnt   = getLikeCount(btn);

        const nextLiked = !prevLiked;
        const nextCnt   = Math.max(0, prevCnt + (nextLiked ? +1 : -1));
        applyLikeVisual(btn, nextLiked);
        setLikeCount(btn, nextCnt);

        (async () => {
            try {
                const { token, header } = getCsrf();
                const headers = { 'Accept': 'application/json' };
                if (token && header) headers[header] = token;

                const res = await fetch(`${CTX}/api/recipes/${encodeURIComponent(uuid)}/like`, {
                    method: 'POST',
                    headers,
                    credentials: 'same-origin',
                    keepalive: true
                });

                const ct = res.headers.get('content-type') || '';
                if (ct.includes('text/html') || res.status === 401) {
                    applyLikeVisual(btn, prevLiked);
                    setLikeCount(btn, prevCnt);
                    location.href = CTX + '/auth/login';
                    return;
                }
                if (!res.ok) { throw new Error('HTTP ' + res.status); }

                const dto = await res.json(); // {liked, likesCount}
                const serverLiked = !!(dto && dto.liked);
                const serverCnt   = (dto && typeof dto.likesCount === 'number') ? dto.likesCount : nextCnt;

                applyLikeVisual(btn, serverLiked);
                setLikeCount(btn, serverCnt);
            } catch (err) {
                console.error(err);
                applyLikeVisual(btn, prevLiked);
                setLikeCount(btn, prevCnt);
            } finally {
                delete btn.dataset.pending;
            }
        })();
    });

    // ===== 팔로우 클릭 (언팔 금지) =====
    document.addEventListener('click', async function(e){
        const btn = e.target.closest('.followbtn-sm[data-user-email]');
        if (!btn) return;

        if (!USER_EMAIL) { location.href = CTX + '/auth/login'; return; }
        if (btn.disabled || btn.classList.contains('is-self')) return;

        const email = btn.getAttribute('data-user-email') || "";
        if (!email) return;

        if (btn.dataset.following === "true") return;

        const { token, header } = getCsrf();
        const headers = { "Accept": "text/plain" };
        if (token && header) headers[header] = token;

        btn.disabled = true;
        try {
            const res = await fetch(CTX + '/api/follow/' + encodeURIComponent(email), {
                method: 'POST',
                headers,
                credentials: 'same-origin'
            });
            if (res.status === 401) { location.href = CTX + '/auth/login'; return; }
            if (!res.ok) { alert('팔로우에 실패했습니다.'); return; }

            applyFollowVisual(btn, true);
            const uid = btn.getAttribute('data-user-id') || "";
            if (uid) {
                const set = await loadMyFollowingSetOnce();
                set.add(uid);
            }
        } catch (err) {
            console.error(err);
            alert('네트워크 오류가 발생했어요.');
        } finally {
            // Following이면 이미 disabled 상태
        }
    });

    // init
    ready(function(){
        setupPopularTags();
        setupForYou();
        setupTrending();
    });
})();
