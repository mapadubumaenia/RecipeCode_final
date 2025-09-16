// /static/js/search-page.js

(() => {
    const $ = (sel, root = document) => root.querySelector(sel);

    // --- DOM targets ---
    const input = $('.search-input');
    const btnSearch = $('.search-btn');
    const sortSelect = $('select.tabs.select-box');
    const resultsAnchor = $('#foryou'); // "Results" 제목 h2

    // 결과 컨테이너를 동적으로 생성해서 Results 제목 바로 뒤에 붙임
    const resultsWrap = document.createElement('div');
    resultsWrap.id = 'results';
    resultsAnchor?.insertAdjacentElement('afterend', resultsWrap);

    // 무한 스크롤용 센티널
    const sentinel = document.createElement('div');
    sentinel.id = 'results-sentinel';
    resultsWrap.insertAdjacentElement('afterend', sentinel);

    // --- state ---
    let state = {
        q: '',
        sort: 'new',     // 'new' | 'hot' | 'rel'
        after: null,     // 커서 기반(new/hot)에서만 사용
        page: 0,         // rel에서만 사용
        size: 12,
        loading: false,
        hasMore: true
    };

    // select 옵션값을 sort로 매핑 (디자인 텍스트는 유지)
    function getSortFromSelect() {
        const v = (sortSelect?.value || '').toLowerCase();
        if (v.includes('like')) return 'hot';
        // 'lastes' 라벨은 그대로 두고 sort는 'new' 로 매핑
        return 'new';
    }

    // 유틸: 안전한 텍스트 출력
    const esc = (v) => String(v ?? '').replace(/[&<>"']/g, m => ({
        '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
    }[m]));

    // 유틸: 상대시간
    function timeAgo(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        if (isNaN(d.getTime())) return '';
        const s = Math.floor((Date.now() - d.getTime()) / 1000);
        if (s < 60) return `${s}s`;
        const m = Math.floor(s/60);
        if (m < 60) return `${m}m`;
        const h = Math.floor(m/60);
        if (h < 24) return `${h}h`;
        const dys = Math.floor(h/24);
        if (dys < 30) return `${dys}d`;
        const mo = Math.floor(dys/30);
        if (mo < 12) return `${mo}mo`;
        const y = Math.floor(mo/12);
        return `${y}y`;
    }

    // 카드 렌더 (디자인 클래스 그대로 사용)
    function renderCard(item) {
        const el = document.createElement('article');
        el.className = 'card p-16 post';

        // 데이터
        const title = esc(item.title);
        const author = esc(item.authorNick || 'anonymous');
        const likes = Number(item.likes || 0);
        const createdAt = item.createdAt ? esc(item.createdAt) : '';
        const ago = item.createdAt ? timeAgo(item.createdAt) : '';

        el.innerHTML = `
      <div class="post-head">
        <div class="avatar-ss"><img src="" alt=""></div>
        <div class="post-info">
          <div class="post-id">@${author}</div>
          <div class="muted">${ago || ''}</div>
        </div>
        <button class="followbtn-sm" data-user-id="${esc(item.authorId || '')}" data-following="false">Follow</button>
      </div>
      <div class="thumb">
        <!-- 이미지는 빈 src 로 -->
        <img src="" alt="${title}">
      </div>
      <p class="muted">${title}</p>
      <div class="post-cta">
        <button class="btn-none">❤️ ${likes}</button>
        <button class="btn-none">💬 0</button>
        <button class="btn-none">↗ Share</button>
      </div>
    `;
        resultsWrap.appendChild(el);
    }

    // 결과 비우기
    function resetResults() {
        resultsWrap.innerHTML = '';
        state.after = null;
        state.page = 0;
        state.hasMore = true;
    }

    // API 호출 URL 구성
    function buildUrl() {
        const params = new URLSearchParams();
        if (state.q) params.set('q', state.q);
        params.set('sort', state.sort);
        params.set('size', state.size);

        // rel 은 page 기반, new/hot 은 after 기반
        if (state.sort === 'rel') {
            params.set('page', state.page);
        } else {
            if (state.after) params.set('after', state.after);
        }
        return `/api/search?${params.toString()}`;
    }

    async function loadMore() {
        if (state.loading || !state.hasMore) return;
        state.loading = true;

        try {
            const url = buildUrl();
            const res = await fetch(url);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();

            const items = Array.isArray(data.items) ? data.items : [];
            for (const it of items) renderCard(it);

            // next/hasMore 처리
            if (state.sort === 'rel') {
                // rel은 커서 없음 → 더 가져온 개수가 page 사이즈보다 작으면 끝
                state.page += 1;
                state.hasMore = items.length >= state.size;
            } else {
                // new/hot 은 커서 사용
                state.after = data.next || null;
                state.hasMore = Boolean(state.after);
            }
        } catch (e) {
            // 오류 시 더 불러오지 않음
            state.hasMore = false;
            // 콘솔만 찍고 UI는 건드리지 않음(디자인 영향 최소화)
            console.warn('[search] load error:', e);
        } finally {
            state.loading = false;
        }
    }

    // 검색 실행 (초기/새 조건)
    function runSearch() {
        state.q = (input?.value || '').trim();
        state.sort = getSortFromSelect();
        resetResults();
        loadMore();
    }

    // 이벤트 바인딩
    btnSearch?.addEventListener('click', runSearch);
    input?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') runSearch();
    });
    sortSelect?.addEventListener('change', runSearch);

    // 무한 스크롤
    if ('IntersectionObserver' in window) {
        const io = new IntersectionObserver((entries) => {
            for (const ent of entries) {
                if (ent.isIntersecting) loadMore();
            }
        }, { rootMargin: '800px 0px' });
        io.observe(sentinel);
    } else {
        // 폴백: 스크롤 하단 근처에서 로드
        window.addEventListener('scroll', () => {
            const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 800;
            if (nearBottom) loadMore();
        });
    }

    // 최초 진입시 기본 검색 실행 (q 비어있고 sort=new → 최신순)
    runSearch();
})();
