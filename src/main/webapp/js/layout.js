// layout.js
(function () {
    // ===== [1] 사이드바 active 정확히 1곳만 =====
    const ctxAttr = document.body.getAttribute('data-ctx') || '';
    const full = location.pathname;
    const path = ctxAttr && full.startsWith(ctxAttr) ? (full.slice(ctxAttr.length) || '/') : full;

    function updateActive() {
        const links = document.querySelectorAll('.nav a[data-path], .nav a[href]');
        links.forEach(a => a.classList.remove('active'));

        let best = null, bestLen = -1;
        links.forEach(a => {
            const base = a.getAttribute('data-path') || new URL(a.href, location.origin).pathname;
            if (!base) return;

            const isRootAdmin = base === '/admin';
            const exact = path === base;
            const prefix = !isRootAdmin && (path === base || path.startsWith(base + '/'));

            if (exact || prefix) {
                if (base.length > bestLen) { best = a; bestLen = base.length; }
            }
        });

        if (best) best.classList.add('active');
    }

    document.addEventListener('DOMContentLoaded', updateActive);
    window.addEventListener('pageshow', updateActive); // 뒤로가기(bfcache) 대응

    // ===== [2] 네비게이션 강제 (필요 시 유지) =====
    const jumpIfNav = (e) => {
        if (e.button !== undefined && e.button !== 0) return;
        if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;

        const link = e.target.closest('.nav a[href]');
        if (!link) return;

        const href = link.href;
        if (!href || location.href === href) return;

        e.stopImmediatePropagation();
        window.location.assign(href);
    };

    document.addEventListener('pointerdown', jumpIfNav, true);
    document.addEventListener('mousedown',   jumpIfNav, true);

    document.addEventListener('keydown', (e) => {
        const link = e.target.closest('.nav a[href]');
        if (!link) return;
        if (e.key !== 'Enter' && e.key !== ' ') return;

        e.stopImmediatePropagation();
        e.preventDefault();
        window.location.assign(link.href);
    }, true);

    document.addEventListener('click', (e) => {
        const link = e.target.closest('.nav a[href]');
        if (!link) return;
        if (location.href === link.href) return;
        e.stopImmediatePropagation();
        e.preventDefault?.();
        window.location.assign(link.href);
    }, true);
})();
