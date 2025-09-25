// =========================
// search.js  (Search page)
// =========================
(function () {
    "use strict";

    // JSP가 내려준 컨텍스트
    const CTX = (typeof window !== "undefined" && window.__CTX__) ? window.__CTX__ : "";

    // DOM ready (defer면 즉시 실행되지만 방어용)
    function ready(fn){
        if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', fn);
        else fn();
    }

    // 유틸
    function esc(s){
        if (s == null) return '';
        return String(s)
            .replace(/&/g,'&amp;').replace(/</g,'&lt;')
            .replace(/>/g,'&gt;').replace(/"/g,'&quot;')
            .replace(/'/g,'&#39;');
    }
    function fmtDate(v) {
        if (!v) return '';
        try {
            var d = new Date(v);
            if (isNaN(d.getTime())) return '';
            var y = d.getFullYear();
            var m = String(d.getMonth() + 1).padStart(2, '0');
            var day = String(d.getDate()).padStart(2, '0');
            return y + '-' + m + '-' + day;
        } catch { return ''; }
    }
    function attachLightYouTube(container){
        if (!container) return;
        var src = container.getAttribute('data-yt-src');
        if (!src) return;
        var iframe = document.createElement('iframe');
        var finalSrc = src + (src.includes('?') ? '&' : '?') + 'autoplay=1&mute=0';
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

    ready(function(){
        // DOM refs
        var $q        = document.getElementById('q');
        var $sort     = document.getElementById('sortSelect');
        var $btn      = document.getElementById('btnSearch');
        var $list     = document.getElementById('results');
        var $sentinel = document.getElementById('resultsSentinel');
        var $trending = document.getElementById('trending'); // 없으면 무시

        // 상태
        var state = { q: '', sort: 'new', next: null, loading: false, size: 5 };

        // URL → 상태
        function seedFromUrl(){
            var params = new URLSearchParams(window.location.search);
            var qParam = params.get('q') || '';
            var sortParam = params.get('sort') || 'new';
            if ($q)    $q.value = qParam;
            if ($sort) $sort.value = sortParam;
            state.q = qParam.trim();
            state.sort = ($sort && $sort.value) ? $sort.value : 'new';
            state.next = null;
        }

        // 상태 → URL
        function syncUrl(){
            var params = new URLSearchParams();
            if (state.q)    params.set('q', state.q);
            if (state.sort) params.set('sort', state.sort);
            var qs = params.toString();
            var url = CTX + '/search' + (qs ? ('?' + qs) : '');
            history.replaceState(null, '', url);
        }

        // 아이템 렌더 (영상/유튜브/이미지 + 태그)
        function renderItem(it){
            try {
                var title = esc(it.title || '');
                var nick  = esc(it.authorNick || '');
                var date  = fmtDate(it.createdAt);
                var likes = (it.likes != null) ? it.likes : 0;
                var cmts  = (it.comments != null) ? it.comments : 0;
                var views = (it.views != null) ? it.views : 0;

                var idOk  = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(it.id || '');
                var href  = idOk ? (CTX + '/recipes/' + encodeURIComponent(it.id)) : '#';

                // 미디어
                var kind = it.mediaKind || 'image';
                var mediaHtml = '';
                if (kind === 'youtube') {
                    var poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
                    var src    = it.mediaSrc || '';
                    mediaHtml =
                        '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                        'aria-label="' + title + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                        '<img src="' + esc(poster) + '" alt="">' +
                        '<div class="play-badge">▶</div>' +
                        '</div>';
                } else if (kind === 'video') {
                    var vsrc   = it.mediaSrc || '';
                    var poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                    mediaHtml =
                        '<div class="media aspect">' +
                        '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                        '</div>';
                } else {
                    var img = (it.mediaSrc && it.mediaSrc.length > 0)
                        ? it.mediaSrc
                        : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
                    mediaHtml =
                        '<div class="media aspect">' +
                        '<img src="' + esc(img) + '" alt="">' +
                        '</div>';
                }

                // 태그
                var tagsHtml = '';
                if (Array.isArray(it.tags) && it.tags.length > 0) {
                    var chips = [];
                    for (var i = 0; i < it.tags.length; i++) {
                        var t = it.tags[i];
                        if (t == null) continue;
                        var txt = esc(String(t));
                        if (!txt) continue;
                        chips.push('<span class="tag">#' + txt + '</span>');
                    }
                    if (chips.length) tagsHtml = '<div class="tags">' + chips.join('') + '</div>';
                }

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
                    mediaHtml +
                    (idOk ? ('<a class="post-link title" href="' + href + '">') : '<div class="post-link disabled" aria-disabled="true">') +
                    '<p class="muted" style="margin-top:8px">' + title + '</p>' +
                    tagsHtml +
                    (idOk ? '</a>' : '</div>') +
                    '<div class="post-cta">' +
                    '<button class="btn-none">❤️ ' + likes + '</button>' +
                    '<button class="btn-none">💬 ' + cmts + '</button>' +
                    '<button class="btn-none" title="views">👁 ' + views + '</button>' +
                    '</div>';

                if (!idOk) {
                    el.querySelector('.post-link.disabled')?.addEventListener('click', function(e){ e.preventDefault(); });
                    console.warn('[search] invalid id:', it.id, 'title=', it.title);
                }
                $list.appendChild(el);
            } catch (e) {
                console.error('[renderItem] failed with item:', it, e);
            }
        }

        // 트렌딩 (있을 경우)
        function renderTrendingItem(it){
            var wrap = document.createElement('article');
            wrap.className = 'card p-12 trend-card';

            var kind  = it.mediaKind || 'image';
            var title = esc(it.title || '');
            var mediaHtml = '';
            if (kind === 'youtube') {
                var poster = it.poster || (it.thumbUrl || 'https://via.placeholder.com/1200x800?text=');
                var src    = it.mediaSrc || '';
                mediaHtml =
                    '<div class="media aspect light-yt" role="button" tabindex="0" ' +
                    'aria-label="' + title + ' 동영상 재생" data-yt-src="' + esc(src) + '">' +
                    '<img src="' + esc(poster) + '" alt="">' +
                    '<div class="play-badge">▶</div>' +
                    '</div>';
            } else if (kind === 'video') {
                var vsrc   = it.mediaSrc || '';
                var poster = it.poster ? (' poster="' + esc(it.poster) + '"') : '';
                mediaHtml =
                    '<div class="media aspect">' +
                    '<video controls preload="metadata"' + poster + ' src="' + esc(vsrc) + '"></video>' +
                    '</div>';
            } else {
                var img = (it.mediaSrc && it.mediaSrc.length > 0)
                    ? it.mediaSrc
                    : ((it.thumbUrl && it.thumbUrl.length > 0) ? it.thumbUrl : 'https://via.placeholder.com/1200x800?text=');
                mediaHtml =
                    '<div class="media aspect">' +
                    '<img src="' + esc(img) + '" alt="">' +
                    '</div>';
            }

            wrap.innerHTML =
                mediaHtml +
                '<div><div class="trend-title">' + title + '</div></div>' +
                '<div class="actions">' +
                '<button class="act-btn">❤️ ' + (it.likes || 0) + '</button>' +
                '<button class="act-btn">💬 ' + (it.comments || 0) + '</button>' +
                '</div>';

            $trending.appendChild(wrap);
        }

        function renderEmpty(q) {
            var msg = q ? '“' + esc(q) + '” 에 대한 결과가 없어요.' : '검색어를 입력해 보세요.';
            $list.innerHTML =
                '<div class="empty">' +
                '<div class="emoji">🔎</div>' +
                '<p><strong>검색 내용이 없습니다.</strong></p>' +
                '<p class="hint">' + msg + '</p>' +
                '</div>';
        }

        // 데이터 호출
        async function fetchTrending() {
            if (!$trending) return; // 섹션 없으면 스킵
            try {
                var url = CTX + '/api/trending?size=8';
                var res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                if (!res.ok) return;
                var data = await res.json();
                $trending.innerHTML = '';
                (data.items || []).forEach(renderTrendingItem);
            } catch (e) {
                console.warn('[trending] load failed', e);
            }
        }

        async function fetchOnce(initial) {
            if (state.loading) return;
            state.loading = true;

            var url = CTX + '/api/search?q=' + encodeURIComponent(state.q) +
                '&sort=' + encodeURIComponent(state.sort) +
                '&size=' + state.size;
            if (!initial && state.next) url += '&after=' + encodeURIComponent(state.next);

            try {
                var res = await fetch(url, { headers: { 'Accept': 'application/json' } });
                if (!res.ok) { state.loading = false; return; }
                var data = await res.json();

                if (initial) $list.innerHTML = '';
                (data.items || []).forEach(renderItem);
                state.next = data.next || null;

                if (initial && (!data.items || data.items.length === 0)) {
                    state.next = null;
                    renderEmpty(state.q);
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

        // 이벤트 바인딩
        if ($btn) $btn.addEventListener('click', startSearch);
        if ($q) $q.addEventListener('keydown', function(e){ if (e.key === 'Enter') startSearch(); });
        if ($sort) $sort.addEventListener('change', startSearch);

        // 라이트 유튜브 위임 이벤트
        document.addEventListener('click', function(e){
            var el = e.target.closest('.light-yt[data-yt-src]');
            if (el) attachLightYouTube(el);
        });
        document.addEventListener('keydown', function(e){
            if (e.key !== 'Enter' && e.key !== ' ') return;
            var el = document.activeElement;
            if (el && el.classList && el.classList.contains('light-yt') && el.hasAttribute('data-yt-src')) {
                e.preventDefault();
                attachLightYouTube(el);
            }
        });

        // 무한 스크롤
        if ($sentinel) {
            var io = new IntersectionObserver(function(entries){
                entries.forEach(function(entry){
                    if (entry.isIntersecting && state.next) fetchOnce(false);
                });
            });
            io.observe($sentinel);
        }

        // 초기 로드
        seedFromUrl();
        fetchTrending();
        startSearch();
    });
})();
