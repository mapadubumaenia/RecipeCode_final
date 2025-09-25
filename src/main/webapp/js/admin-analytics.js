// admin-analytics.js
(function () {
    const ctxPath = (typeof window.APP_CTX === 'string') ? window.APP_CTX : '';
    const api = (p) => ctxPath + '/api/admin/analytics' + p;

    const $  = (s) => document.querySelector(s);
    const fmtDate = (iso) => iso ? new Date(iso).toISOString().slice(0, 10) : '';

    async function getJson(url) {
        const r = await fetch(url, { headers: { 'Accept': 'application/json' } });
        if (!r.ok) throw new Error(`${r.status}`);
        return r.json();
    }

    // 1) 제로결과 키워드
    const zkBtn = $('#zkLoad');
    if (zkBtn) {
        zkBtn.onclick = async () => {
            $('#zkErr').textContent = '';
            const from = $('#zkFrom').value ? new Date($('#zkFrom').value).toISOString() : '';
            const to   = $('#zkTo').value   ? new Date($('#zkTo').value).toISOString()   : '';
            const size = $('#zkSize').value || 20;
            let qs = `?size=${encodeURIComponent(size)}`;
            if (from) qs += `&from=${encodeURIComponent(from)}`;
            if (to)   qs += `&to=${encodeURIComponent(to)}`;
            try {
                const data = await getJson(api('/zero-keywords' + qs));
                const tbody = $('#zkTbody'); tbody.innerHTML = '';
                data.forEach((row, i) => {
                    const tr = document.createElement('tr');
                    const q = row.keyword || '';
                    tr.innerHTML = `
            <td>${i + 1}</td>
            <td><a href="${ctxPath}/search?q=${encodeURIComponent(q)}" target="_blank">${q || '(빈문자열)'}</a></td>
            <td>${row.count ?? 0}</td>
            <td>${row.lastAt ? fmtDate(row.lastAt) : '-'}</td>
          `;
                    tbody.appendChild(tr);
                });
            } catch (e) { $('#zkErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 2) Top Viewed
    const tvBtn = $('#tvLoad');
    if (tvBtn) {
        tvBtn.onclick = async () => {
            $('#tvErr').textContent = '';
            const days = $('#tvDays').value || 7;
            const size = $('#tvSize').value || 10;
            try {
                const data = await getJson(api(`/top-viewed?days=${days}&size=${size}`));
                const tbody = $('#tvTbody'); tbody.innerHTML = '';
                data.forEach((d, i) => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
            <td>${i + 1}</td>
            <td><a href="${ctxPath}/recipes/${encodeURIComponent(d.id)}" target="_blank">${d.title || ''}</a></td>
            <td>${d.authorNick || ''}</td>
            <td>${d.views || 0}</td>
            <td>${d.likes || 0}</td>
            <td>${d.createdAt ? fmtDate(d.createdAt) : ''}</td>
          `;
                    tbody.appendChild(tr);
                });
            } catch (e) { $('#tvErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 3) Trending Tags
    const ttBtn = $('#ttLoad');
    if (ttBtn) {
        ttBtn.onclick = async () => {
            $('#ttErr').textContent = '';
            const days = $('#ttDays').value || 30;
            const size = $('#ttSize').value || 20;
            try {
                const data = await getJson(api(`/trending-tags?days=${days}&size=${size}`));
                const box = $('#ttWrap'); box.innerHTML = '';
                data.forEach(t => {
                    const a = document.createElement('a');
                    a.className = 'chip';
                    a.href = `${ctxPath}/search?tags=${encodeURIComponent(t.tag)}`;
                    a.textContent = `#${t.tag} (${t.count})`;
                    a.target = '_blank';
                    box.appendChild(a);
                });
            } catch (e) { $('#ttErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 4) Top Liked
    const tlBtn = $('#tlLoad');
    if (tlBtn) {
        tlBtn.onclick = async () => {
            $('#tlErr').textContent = '';
            const days = $('#tlDays').value || 7;
            const size = $('#tlSize').value || 10;
            try {
                const data = await getJson(api(`/top-liked?days=${days}&size=${size}`));
                const tbody = $('#tlTbody'); tbody.innerHTML = '';
                data.forEach((d, i) => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
            <td>${i + 1}</td>
            <td><a href="${ctxPath}/recipes/${encodeURIComponent(d.id)}" target="_blank">${d.title || ''}</a></td>
            <td>${d.authorNick || ''}</td>
            <td>${d.likes || 0}</td>
            <td>${d.views || 0}</td>
            <td>${d.createdAt ? fmtDate(d.createdAt) : ''}</td>
          `;
                    tbody.appendChild(tr);
                });
            } catch (e) { $('#tlErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 5) Uploads by Day (Chart)
    let udChart;
    const udBtn = $('#udLoad');
    if (udBtn) {
        udBtn.onclick = async () => {
            $('#udErr').textContent = '';
            const days = $('#udDays').value || 30;
            try {
                const data = await getJson(api(`/uploads-by-day?days=${days}`));
                const labels = data.map(d => d.date);
                const counts = data.map(d => d.count);
                const ctx = document.getElementById('udChart').getContext('2d');
                if (udChart) udChart.destroy();
                udChart = new Chart(ctx, {
                    type: 'line',
                    data: { labels, datasets: [{ label: '업로드 수', data: counts }] },
                    options: {
                        responsive: true,
                        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
                        plugins: { legend: { display: false } }
                    }
                });
            } catch (e) { $('#udErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 6) Top Creators
    const tcBtn = $('#tcLoad');
    if (tcBtn) {
        tcBtn.onclick = async () => {
            $('#tcErr').textContent = '';
            const days = $('#tcDays').value || 30;
            const size = $('#tcSize').value || 10;
            try {
                const data = await getJson(api(`/top-creators?days=${days}&size=${size}`));
                const tbody = $('#tcTbody'); tbody.innerHTML = '';
                data.forEach((d, i) => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
            <td>${i + 1}</td>
            <td>${d.authorNick || ''}</td>
            <td>${d.posts || 0}</td>
            <td>${d.sumLikes || 0}</td>
            <td>${d.sumViews || 0}</td>
          `;
                    tbody.appendChild(tr);
                });
            } catch (e) { $('#tcErr').textContent = '로드 실패: ' + e.message; }
        };
    }

    // 초기 로드 트리거
    $('#zkLoad')?.click();
    $('#tvLoad')?.click();
    $('#tlLoad')?.click();
    $('#ttLoad')?.click();
    $('#udLoad')?.click();
    $('#tcLoad')?.click();
})();
