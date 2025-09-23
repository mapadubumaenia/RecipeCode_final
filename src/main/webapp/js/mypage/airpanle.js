// 기준 (간단 3단계) 두 수치 중 더 나쁜 쪽으로 판단
// 좋음(야외 굿): PM2.5 ≤ 15 그리고 PM10 ≤ 30
// 주의(마스크 추천): PM2.5 16–35 혹은 PM10 31–80
// 나쁨(집콕 추천): PM2.5 ≥ 36 혹은 PM10 ≥ 81


(function () {
    const box = document.getElementById('airBox');
    if (!box) return;

    const ctx = window.ctx || box.dataset.ctx || "";
    const txt = document.getElementById('airText');
    const sidoSel = document.getElementById('sido');

    function toNum(v) {
        const n = parseFloat(v);
        return Number.isFinite(n) ? n : null;
    }

    function avg(items, key) {
        const arr = items.map(it => toNum(it[key])).filter(n => n !== null);
        if (!arr.length) return null;
        const sum = arr.reduce((a, b) => a + b, 0);
        return Math.round(sum / arr.length);
    }

    function advice(pm10, pm25) {
        const p10 = Number(pm10);
        const p25 = Number(pm25);

        if (!Number.isFinite(p10) && !Number.isFinite(p25)) {
            return { label: '정보 부족', emoji: '❔', color: '#666' };
        }
        const bad = (Number.isFinite(p25) && p25 >= 36) || (Number.isFinite(p10) && p10 >= 81);
        const mid = (Number.isFinite(p25) && p25 >= 16) || (Number.isFinite(p10) && p10 >= 31);

        if (bad) return { label: '집에 있는 게 좋은 날', emoji: '😷', color: '#e23' };
        if (mid) return { label: '가벼운 마스크 추천', emoji: '🙂', color: '#e8b300' };
        return { label: '야외 활동하기 좋은 날', emoji: '🌤️', color: '#1eaa6a' };
    }

    function grade(v) {
        const n = Number(v);
        if (!Number.isFinite(n)) return '정보없음';
        if (n <= 30) return '좋음 🌱';
        if (n <= 80) return '보통 🙂';
        if (n <= 150) return '나쁨 😷';
        return '매우나쁨 ☠️';
    }

    function latestTime(items) {
        const times = items.map(it => it.dataTime).filter(Boolean);
        if (!times.length) return '';
        return times.sort().slice(-1)[0];
    }

    async function loadAir(sido) {
        txt.textContent = '불러오는 중…';
        txt.style.whiteSpace = 'pre-line'; // ← 줄바꿈 보이게!
        try {
            const url = ctx + "/api/air/now?sido=" + encodeURIComponent(sido);
            const resp = await fetch(url, { credentials: "include" });
            if (!resp.ok) throw new Error('fetch 실패: ' + resp.status);

            const data = await resp.json();
            const items = (data && data.response && data.response.body && data.response.body.items) || [];
            if (!items.length) { txt.textContent = '데이터 없음'; return; }

            const pm10Avg = avg(items, "pm10Value"); // 시·도 평균
            const pm25Avg = avg(items, "pm25Value");
            const when    = latestTime(items);
            const count   = items.length;

            const g10 = grade(pm10Avg);
            const g25 = grade(pm25Avg);
            const tip = advice(pm10Avg, pm25Avg);     // ← 여기서 계산!

            txt.textContent =
                sido + ' · ' + when + ' 기준\n' +
                '측정소 ' + count + '곳\n' +
                'PM10 ' + (pm10Avg ?? '-') + ' (' + g10 + ')' + '\n' +
                ' PM2.5 ' + (pm25Avg ?? '-') + ' (' + g25 + ')\n' +
                '➡ ' + tip.emoji + '  ' + tip.label;

        } catch (e) {
            console.error(e);
            txt.textContent = '불러오기 실패';
        }
    }

    loadAir(sidoSel.value);
    sidoSel.addEventListener('change', () => loadAir(sidoSel.value));
})();