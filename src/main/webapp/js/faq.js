(function(){
    const ctx = window.APP_CTX || '';
    async function load(){
        try{
            const r = await fetch(`${ctx}/api/admin/faq`, {headers:{'Accept':'application/json'}});
            const list = r.ok ? (await r.json()) : [];
            const tb = document.getElementById('faqTbody'); tb.innerHTML='';
            list.forEach((it,i)=>{
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>${i+1}</td>
          <td>${it.question||''}</td>
          <td>${it.visible? 'ON':'OFF'}</td>
          <td>${it.orderNo ?? ''}</td>
          <td>
            <button class="btn small" data-id="${it.id}" data-act="edit">수정</button>
            <button class="btn small" data-id="${it.id}" data-act="del">삭제</button>
          </td>`;
                tb.appendChild(tr);
            });
        }catch(e){ document.getElementById('faqErr').textContent='로드 실패'; }
    }
    document.getElementById('faqAdd')?.addEventListener('click', ()=>{/* TODO: 모달 열기 */});
    load();
})();
