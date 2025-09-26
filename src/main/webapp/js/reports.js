(function(){
    const ctx = window.APP_CTX || '';
    const $ = s => document.querySelector(s);
    async function load(){
        $('#rpErr').textContent='';
        const q = new URLSearchParams({
            from: $('#rpFrom').value ? new Date($('#rpFrom').value).toISOString() : '',
            to:   $('#rpTo').value   ? new Date($('#rpTo').value).toISOString()   : '',
            type: $('#rpType').value || '',
            state:$('#rpState').value|| '',
            size: '50'
        });
        try{
            // TODO: 백엔드 구현 후 엔드포인트 교체
            const res = await fetch(`${ctx}/api/admin/moderation/reports?`+q, {headers:{'Accept':'application/json'}});
            if(!res.ok) throw new Error(res.status);
            const rows = await res.json();
            const tb = $('#rpTbody'); tb.innerHTML='';
            rows.forEach((r,i)=>{
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>${i+1}</td>
          <td><a target="_blank" href="${ctx}/recipes/${encodeURIComponent(r.recipeId||'')}">${r.recipeId||''}</a></td>
          <td>${r.type||''}</td>
          <td>${r.reporter||''}</td>
          <td>${r.reason||''}</td>
          <td>${r.state||''}</td>
          <td>${r.createdAt ? new Date(r.createdAt).toLocaleString() : ''}</td>
          <td>
            <button class="btn small" data-act="resolve" data-id="${r.id}">완료</button>
            <button class="btn small" data-act="ban" data-id="${r.id}">작성자 제재</button>
          </td>`;
                tb.appendChild(tr);
            });
        }catch(e){ $('#rpErr').textContent = '로드 실패: '+e.message; }
    }
    $('#rpLoad')?.addEventListener('click', load);
})();
