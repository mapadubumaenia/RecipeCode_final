// =========================
// search.js  (Search page)
// =========================
(function () {
    "use strict";

    const CTX = (typeof window !== "undefined" && window.__CTX__) ? window.__CTX__ : "";
    const USER_EMAIL = (typeof window !== "undefined" && window.__USER_EMAIL__) ? String(window.__USER_EMAIL__).trim().toLowerCase() : "";

    function ready(fn){ if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn); else fn(); }
     function esc(s){
           if (s == null) return '';
           return String(s)
                 .replace(/&/g,'&amp;')
             .replace(/</g,'&lt;')
             .replace(/>/g,'&gt;')
             .replace(/"/g,'&quot;')
             .replace(/'/g,'&#39;');
         }
    function fmtDate(v) {
        if (!v) return '';
        try { const d = new Date(v); if (isNaN(d.getTime())) return ''; return d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0'); } catch { return ''; }
    }
    function isUuid36(s){ return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(s || ''); }
    function pickUuid(it){
        const a = (it && typeof it.uuid === 'string') ? it.uuid : '';
        const b = (it && typeof it.id === 'string')   ? it.id   : '';
        if (isUuid36(a)) return a;
        if (isUuid36(b)) return b;
        return null;
    }

    // ISO 문자열 / epoch(초|ms) / Date → Date
    function toDate(input){
        if (input == null) return null;
        if (input instanceof Date) return isNaN(input.getTime()) ? null : input;
        if (typeof input === 'number') return new Date(input > 1e12 ? input : input*1000);
        const s = String(input).trim();
        if (/^\d+$/.test(s)) { const n = Number(s); return new Date(n > 1e12 ? n : n*1000); }
        const d = new Date(s);
        return isNaN(d.getTime()) ? null : d;
    }

// YYYY-MM-DD HH:mm (로컬)
    function fmtYmdHm(v){
        const d = toDate(v); if (!d) return '';
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
    async function hydrateMyLikesIn(container){
        const btns = Array.from(container.querySelectorAll('.js-like[data-uuid]'))
            .filter(b => !b.hasAttribute('disabled'));
        if (btns.length === 0 || !USER_EMAIL) return;
        const ids = Array.from(new Set(btns.map(b => b.getAttribute('data-uuid')).filter(Boolean)));
        if (ids.length === 0) return;

        const CHUNK = 50;
        for (let i=0;i<ids.length;i+=CHUNK){
            const chunk = ids.slice(i, i+CHUNK);
            try{
                const res = await fetch(CTX + '/api/recipes/likes/mine?ids=' + encodeURIComponent(chunk.join(',')), {
                    headers: { 'Accept':'application/json' }, credentials:'same-origin'
                });
                if (!res.ok) throw new Error('batch http '+res.status);
                const body = await res.json();
                const map = (body && body.map) || {};
                chunk.forEach(id=>{
                    const liked = !!map[id];
                    container.querySelectorAll(`.js-like[data-uuid="${CSS.escape(id)}"]`).forEach(btn=>{
                        applyLikeVisual(btn, liked);
                    });
                });
            }catch{
                // 폴백: 단건 조회
                await Promise.all(chunk.map(async (id)=>{
                    try{
                        const r = await fetch(`${CTX}/api/recipes/${encodeURIComponent(id)}/like/status`, {
                            headers:{'Accept':'application/json'}, credentials:'same-origin'
                        });
                        if (!r.ok) return;
                        const b = await r.json();
                        const liked = !!b?.liked;
                        container.querySelectorAll(`.js-like[data-uuid="${CSS.escape(id)}"]`).forEach(btn=>{
                            applyLikeVisual(btn, liked);
                        });
                    }catch{}
                }));
            }
        }
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
            img.onerror = function(){ this.onerror = null; // 무한 루프 방지
                this.src = CTX + "/images/default_profile.jpg"; }; // 404 → 비우기
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

    // ===== 팔로잉 세트 =====
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
        btn.dataset.following = isFollowing ? 'true' : 'false';
        btn.classList.toggle('is-active', !!isFollowing);
        btn.textContent = isFollowing ? 'Following' : 'Follow';
        btn.disabled = !!isFollowing; // 언팔 금지
        if (isFollowing) btn.title = "이미 팔로우 중";
    }
    async function hydrateFollowButtonsIn(container){
        const set = await loadMyFollowingSetOnce();
        container.querySelectorAll('.followbtn-sm[data-user-id]').forEach(btn => {
            const uid = btn.getAttribute('data-user-id') || "";
            applyFollowVisual(btn, set.has(uid));
        });
    }

    ready(function(){
        // DOM refs
        const $q        = document.getElementById('q');
        const $sort     = document.getElementById('sortSelect');
        const $btn      = document.getElementById('btnSearch');
        const $list     = document.getElementById('results');
        const $sentinel = document.getElementById('resultsSentinel');
        const $trending = document.getElementById('trending'); // 없으면 무시

        // 상태
        const state = { q: '', sort: 'new', next: null, loading: false, size: 5 };

        function seedFromUrl(){
            const params = new URLSearchParams(window.location.search);
            const qParam = params.get('q') || '';
            const sortParam = params.get('sort') || 'new';
            if ($q)    $q.value = qParam;
            if ($sort) $sort.value = sortParam;
            state.q = qParam.trim();
            state.sort = ($sort && $sort.value) ? $sort.value : 'new';
            state.next = null;
        }

        function syncUrl(){
            const params = new URLSearchParams();
            if (state.q)    params.set('q', state.q);
            if (state.sort) params.set('sort', state.sort);
            const qs = params.toString();
            const url = CTX + '/search' + (qs ? ('?' + qs) : '');
            history.replaceState(null, '', url);
        }

        function renderItem(it){
            const title = esc(it.title || '');
            const created = fmtYmdHm(it.createdAt);
            const likes = (it.likes != null) ? it.likes : 0;
            const cmts  = (it.comments != null) ? it.comments : 0;
            const views = (it.views != null) ? it.views : 0;

            // 작성자 id/email
            const authorRawId = it.authorId || it.authorNick || '';
            const cleanId = (authorRawId && authorRawId.startsWith('@')) ? authorRawId.substring(1) : (authorRawId || '');
            const userIdAttr = cleanId ? ('@' + cleanId) : '';
            const profileHref = cleanId ? (CTX + '/follow/profile/' + encodeURIComponent(cleanId)) : '#';
            const authorEmail = (it.authorEmail || '').trim().toLowerCase();
            const self = (USER_EMAIL && authorEmail && USER_EMAIL === authorEmail);

            // 상세/링크용 uuid
            const uuid = pickUuid(it);
            const idOk = !!uuid;
            const href = idOk ? (CTX + '/recipes/' + encodeURIComponent(uuid)) : '#';

            // 미디어
            const kind = it.mediaKind || 'image';
            let mediaHtml = '';
            if (kind === 'youtube') {
                const poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
                const src    = it.mediaSrc || '';
                mediaHtml =
                    '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                    'aria-label="' + title + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                    '<img src="' + esc(poster) + '" alt="">' +
                    '<div class="play-badge">▶</div>' +
                    '</div>';
            } else if (kind === 'video') {
                const vsrc   = it.mediaSrc || '';
                const poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                mediaHtml =
                    '<div class="media aspect">' +
                    '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                    '</div>';
            } else {
                const img = (it.mediaSrc && it.mediaSrc.length > 0)
                    ? it.mediaSrc
                    : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
                mediaHtml =
                    '<div class="media aspect">' +
                    '<img src="' + esc(img) + '" alt="">' +
                    '</div>';
            }

            // 태그
            let tagsHtml = '';
            if (Array.isArray(it.tags) && it.tags.length > 0) {
                const chips = it.tags.map(t => '<span class="tag">#' + esc(String(t)) + '</span>').join('');
                tagsHtml = '<div class="tags">' + chips + '</div>';
            }

            const el = document.createElement('article');
            el.className = 'card p-16 post';
            el.innerHTML =
                '<div class="post-head">' +
                '  <div class="avatar-ss"><img src="" alt="" data-user-id="' + esc(userIdAttr) + '"></div>' +
                '  <div class="post-info">' +
                '    <div class="post-id">' + (cleanId ? ('<a class="author-link" href="' + profileHref + '">@' + esc(cleanId) + '</a>') : '') + '</div>' +
                 '    <div class="muted">' +
                 '      <time datetime="' + esc(it.createdAt || '') + '">' + esc(created) + '</time>' +
                 '    </div>' +
                '  </div>' +
                '  <button class="followbtn-sm' + (self ? ' is-self' : '') + '"' +
                '     data-user-id="' + esc(userIdAttr) + '"' +
                '     data-user-email="' + esc(authorEmail) + '"' +
                '     data-following="false"' +
                (self ? ' disabled' : '') + '>' +
                (self ? 'Me' : 'Follow') +
                '  </button>' +
                '</div>' +
                mediaHtml +
                (idOk ? ('<a class="post-link title" href="' + href + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                '  <p class="muted" style="margin-top:8px">' + title + '</p>' +
                tagsHtml +
                (idOk ? '</a>' : '</div>') +
                '<div class="post-cta">' +
                '  <button class="btn-none js-like" ' +
                (idOk && !self
                    ? ('data-uuid="' + esc(uuid) + '" ')
                    : 'disabled aria-disabled="true" title="' + (self ? '내 게시물은 좋아요를 누를 수 없어요' : '이 카드엔 uuid가 없어요') + '" ') +
                '      data-liked="false" aria-pressed="false">' +
                '    ❤️ <span class="like-count">' + likes + '</span>' +
                '  </button>' +
                '  <button class="btn-none">💬 ' + cmts + '</button>' +
                '  <button class="btn-none" title="views">👁 ' + views + '</button>' +
                '</div>';

            if (!idOk) el.querySelector('.post-link.disabled')?.addEventListener('click', function(e){ e.preventDefault(); });
            document.getElementById('results').appendChild(el);
        }

        // 트렌딩 (있을 경우)
        async function fetchTrending() {
            if (!$trending) return;
            try {
                const url = CTX + '/api/trending?size=8';
                const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                if (!res.ok) return;
                const data = await res.json();
                $trending.innerHTML = '';
                (data.items || []).forEach(function(it){
                    const title = esc(it.title || '');
                    const kind  = it.mediaKind || 'image';
                    let mediaHtml = '';
                    if (kind === 'youtube') {
                        const poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
                        const src    = it.mediaSrc || '';
                        mediaHtml =
                            '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                            'aria-label="' + title + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                            '<img src="' + esc(poster) + '" alt="">' +
                            '<div class="play-badge">▶</div>' +
                            '</div>';
                    } else if (kind === 'video') {
                        const vsrc   = it.mediaSrc || '';
                        const poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                        mediaHtml =
                            '<div class="media aspect">' +
                            '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                            '</div>';
                    } else {
                        const img = (it.mediaSrc && it.mediaSrc.length > 0)
                            ? it.mediaSrc
                            : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
                        mediaHtml =
                            '<div class="media aspect">' +
                            '<img src="' + esc(img) + '" alt="">' +
                            '</div>';
                    }

                    const el = document.createElement('article');
                    el.className = 'card p-12 trend-card';
                    el.innerHTML =
                        mediaHtml +
                        '<div><div class="trend-title">' + title + '</div></div>' +
                        '<div class="actions">' +
                        '<button class="act-btn">❤️ ' + (it.likes || 0) + '</button>' +
                        '<button class="act-btn">💬 ' + (it.comments || 0) + '</button>' +
                        '</div>';
                    $trending.appendChild(el);
                });
            } catch (e) {
                console.warn('[trending] load failed', e);
            }
        }

        async function fetchOnce(initial) {
            if (state.loading) return;
            state.loading = true;

            let url = CTX + '/api/search?q=' + encodeURIComponent(state.q) +
                '&sort=' + encodeURIComponent(state.sort) +
                '&size=' + state.size;
            if (!initial && state.next) url += '&after=' + encodeURIComponent(state.next);

            try {
                const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                if (!res.ok) { state.loading = false; return; }
                const data = await res.json();

                if (initial) $list.innerHTML = '';
                (data.items || []).forEach(renderItem);

                // 렌더 후 팔로우/아바타/내 좋아요 하이드레이션
                await hydrateFollowButtonsIn($list);
                hydrateAvatarsIn($list);
                await hydrateMyLikesIn($list);

                state.next = data.next || null;

                if (initial && (!data.items || data.items.length === 0)) {
                    state.next = null;
                    const msg = state.q ? '“' + esc(state.q) + '” 에 대한 결과가 없어요.' : '검색어를 입력해 보세요.';
                    $list.innerHTML =
                        '<div class="empty">' +
                        '<div class="emoji">🔎</div>' +
                        '<p><strong>검색 내용이 없습니다.</strong></p>' +
                        '<p class="hint">' + msg + '</p>' +
                        '</div>';
                }
            } finally {
                state.loading = false;
            }
        }

        function startSearch() {
            state.q    = ($q && $q.value ? $q.value : '').trim();
            state.sort = ($sort && $sort.value) ? $sort.value : 'new';
            state.next = null;
            syncUrl();
            fetchOnce(true);
        }

        if ($btn) $btn.addEventListener('click', startSearch);
        if ($q) $q.addEventListener('keydown', function(e){ if (e.key === 'Enter') startSearch(); });
        if ($sort) $sort.addEventListener('change', startSearch);

        document.addEventListener('click', function(e){
            const el = e.target.closest('.light-yt[data-yt-src]');
            if (el) attachLightYouTube(el);
        });
        document.addEventListener('keydown', function(e){
            if (e.key !== 'Enter' && e.key !== ' ') return;
            const el = document.activeElement;
            if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
                e.preventDefault();
                attachLightYouTube(el);
            }
        });

        if ($sentinel) {
            const io = new IntersectionObserver(function(entries){
                entries.forEach(function(entry){
                    if (entry.isIntersecting && state.next) fetchOnce(false);
                });
            }, { rootMargin: '600px 0px' });
            io.observe($sentinel);
        }

        // =========================
        //  좋아요 토글 (낙관적 UI)
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
                    const headers = { 'Accept':'application/json' };
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

                    const dto = await res.json();
                    const serverLiked = !!(dto && dto.liked);
                    const serverCnt   = (dto && typeof dto.likesCount === 'number') ? dto.likesCount : nextCnt;

                    applyLikeVisual(btn, serverLiked);
                    setLikeCount(btn, serverCnt);

                } catch (err){
                    console.error('[like] failed, rollback:', err);
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
                // Following이면 그대로 disabled 유지
            }
        });

        // 초기 로드
        seedFromUrl();
        fetchTrending();
        startSearch();
    });
})();
