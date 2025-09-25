(function(){
    const ctx = window.APP_CTX || '';
    async function json(u){ const r=await fetch(u,{headers:{'Accept':'application/json'}}); if(!r.ok) throw new Error(r.status); return r.json(); }
    (async ()=>{
        try{
            const up = await json(`${ctx}/api/admin/analytics/uploads-by-day?days=7`);
            document.getElementById('kpiUploads').textContent = up.reduce((a,b)=>a+(b.count||0),0);

            const tv = await json(`${ctx}/api/admin/analytics/top-viewed?days=7&size=100`);
            document.getElementById('kpiViews').textContent = (tv||[]).reduce((a,b)=>a+(b.views||0),0);

            const from7 = new Date(Date.now()-7*864e5).toISOString();
            const zk = await json(`${ctx}/api/admin/analytics/zero-keywords?size=200&from=${encodeURIComponent(from7)}`);
            document.getElementById('kpiZero').textContent = zk.length;

            // TODO: 신고 미처리 건수 API 연동
            document.getElementById('kpiReports').textContent = '-';
        }catch(e){ console.warn('[overview]', e); }
    })();
})();


(async function(){
    // ----- 데이터 로드 (24h, 1h 간격) -----
    const to = new Date().toISOString();
    const from = new Date(Date.now() - 24*60*60*1000).toISOString();
    const url = `/api/admin/analytics/traffic?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&interval=1h`;
    const res = await fetch(url, { headers: {Accept:'application/json'} });
    const rows = res.ok ? await res.json() : [];

    const data = rows.map(r => ({
        t: new Date(r.ts),
        v: Number(r.views||0)
    }));
    if (!data.length) return;

    // ----- 캔버스/스케일 세팅 -----
    const wrap = document.getElementById('sparkWrap');
    const tip  = document.getElementById('sparkTip');
    const canvas = document.getElementById('spark');
    const dpr = window.devicePixelRatio || 1;

    function resize(){
        const cssW = wrap.clientWidth;
        canvas.width  = Math.round(cssW * dpr);
        canvas.height = Math.round(110 * dpr);
        canvas.style.width  = cssW + 'px';
        canvas.style.height = '110px';
        draw();
    }
    window.addEventListener('resize', resize);

    const pad = {l:14, r:8, t:14, b:18};        // 여백(px, CSS 기준)
    const fmtTime = new Intl.DateTimeFormat('ko-KR', { month:'numeric', day:'numeric', hour:'2-digit' });
    const fmtHM   = new Intl.DateTimeFormat('ko-KR', { hour:'2-digit', minute:'2-digit' });

    // 스케일 계산
    const minT = data[0].t.getTime();
    const maxT = data[data.length-1].t.getTime();
    const minV = 0;
    const maxV = Math.max(...data.map(d => d.v), 1);

    function xScale(ms){
        const w = canvas.width/dpr, usable = w - pad.l - pad.r;
        return (pad.l + ( (ms - minT) / (maxT - minT || 1) ) * usable) * dpr;
    }
    function yScale(v){
        const h = canvas.height/dpr, usable = h - pad.t - pad.b;
        // 값 클수록 위로
        return (pad.t + (1 - (v - minV)/(maxV - minV || 1)) * usable) * dpr;
    }

    // ----- 그리기 -----
    const ctx = canvas.getContext('2d');
    function draw(hoverIdx = -1){
        const w = canvas.width, h = canvas.height;
        ctx.clearRect(0,0,w,h);

        // 영역 채우기(부드럽게)
        ctx.beginPath();
        data.forEach((d,i)=>{
            const x = xScale(d.t.getTime()), y = yScale(d.v);
            if(i===0) ctx.moveTo(x,y);
            else ctx.lineTo(x,y);
        });
        ctx.lineTo(xScale(maxT), yScale(0));
        ctx.lineTo(xScale(minT), yScale(0));
        ctx.closePath();
        ctx.fillStyle = 'rgba(34,197,94,0.12)'; // 연한 초록
        ctx.fill();

        // 라인
        ctx.beginPath();
        data.forEach((d,i)=>{
            const x = xScale(d.t.getTime()), y = yScale(d.v);
            if(i===0) ctx.moveTo(x,y);
            else ctx.lineTo(x,y);
        });
        ctx.lineWidth = 2*dpr;
        ctx.strokeStyle = '#22c55e';
        ctx.stroke();

        // 호버 포커스
        if(hoverIdx >= 0){
            const d = data[hoverIdx];
            const x = xScale(d.t.getTime()), y = yScale(d.v);

            // 가이드 라인
            ctx.beginPath();
            ctx.moveTo(x, yScale(0));
            ctx.lineTo(x, y);
            ctx.setLineDash([4*dpr, 4*dpr]);
            ctx.strokeStyle = 'rgba(17,24,39,0.3)';
            ctx.lineWidth = 1*dpr;
            ctx.stroke();
            ctx.setLineDash([]);

            // 점
            ctx.beginPath();
            ctx.arc(x,y,3.5*dpr,0,Math.PI*2);
            ctx.fillStyle = '#111827';
            ctx.fill();
        }
    }

    // ----- 호버/툴팁 -----
    let dot = null;
    function ensureDot(){
        if (dot) return dot;
        dot = document.createElement('div');
        dot.className = 'spark-dot';
        wrap.appendChild(dot);
        return dot;
    }
    function nearestIdx(mxCss){
        // mxCss: 래퍼 기준 CSS px
        const mx = mxCss * dpr;
        let best = 0, bestDist = Infinity;
        for (let i=0;i<data.length;i++){
            const x = xScale(data[i].t.getTime());
            const dist = Math.abs(mx - x);
            if (dist < bestDist){ best=i; bestDist=dist; }
        }
        return best;
    }

    canvas.addEventListener('mousemove', (e)=>{
        const rect = canvas.getBoundingClientRect();
        const mxCss = e.clientX - rect.left;
        const idx = nearestIdx(mxCss);
        draw(idx);

        const d = data[idx];
        const xCss = Math.round(xScale(d.t.getTime())/dpr);
        const yCss = Math.round(yScale(d.v)/dpr);

        const tt = `${fmtTime.format(d.t)} · ${d.v.toLocaleString()}건`;
        tip.textContent = tt;
        tip.hidden = false;
        tip.style.left = xCss + 'px';
        tip.style.top  = yCss + 'px';

        const el = ensureDot();
        el.style.left = xCss + 'px';
        el.style.top  = yCss + 'px';
    });
    canvas.addEventListener('mouseleave', ()=>{
        tip.hidden = true;
        if (dot) dot.remove(), dot=null;
        draw(-1);
    });

    resize(); // 최초 렌더
})();
