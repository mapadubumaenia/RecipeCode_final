(function(){
    // JSP가 내려준 컨텍스트(없으면 빈 문자열)
    const CTX = (typeof window !== 'undefined' && window.__CTX__) ? window.__CTX__ : '';

    // DOM이 준비되기 전에 실행될 수 있으니, defer를 쓰지 않는다면 DOMContentLoaded 사용
    function ready(fn){
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', fn);
        } else {
            fn();
        }
    }

    ready(function(){
        const $wrap = document.getElementById('popularTagsWrap');
        if (!$wrap) return;

        const fmt = new Intl.NumberFormat('en', { notation: 'compact', maximumFractionDigits: 1 });

        function esc(s){
            return (s==null?'':String(s))
                .replace(/&/g,'&amp;').replace(/</g,'&lt;')
                .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');
        }

        async function loadTrendingTags(days=30, size=12){
            const url = CTX + '/api/trends/tags?days=' + encodeURIComponent(days)
                + '&size=' + encodeURIComponent(size);
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
                // 외부 JS 파일이라 백틱 사용해도 JSP-EL 충돌 없음
                node.innerHTML = `<span>#${esc(tag)}</span><span class="chip">${esc(fmt.format(cnt))}</span>`;
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
    });
})();
