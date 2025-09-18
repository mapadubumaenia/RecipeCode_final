// /js/recipe-create-update.js
(function(){
    // === 중복 로딩 가드 ===
    if (window.__recipeInit) {
        console.warn('[recipe] script already initialized (second load blocked)');

        return;
    }
    window.__recipeInit = true;
    window.__recipeLoadCount = (window.__recipeLoadCount || 0) + 1;
    console.log('[recipe] init, loadCount=', window.__recipeLoadCount);

    // === 유틸 ===
    const $  = (s, el=document) => el.querySelector(s);
    const $$ = (s, el=document) => [...el.querySelectorAll(s)];

    // ✅ jQuery 객체 핸들(Validation은 jQuery 필요)
    const $jq = window.jQuery;

    // === 엘리먼트 캐시 ===
    const form            = $('#recipeForm');
    const steps           = $('#steps');
    const ingredientsWrap = $('#ingredients');
    const tagList         = $('#tagList');
    const tagHidden       = $('#tagHidden');

    const postStatus      = $('#postStatus');
    const isPublic        = $('#isPublic');
    const recipeTypeEl    = $('#recipeType');

    const tabImage        = $('#tabImage');
    const tabVideo        = $('#tabVideo');
    const thumbPane       = $('#thumbPane');
    const videoPane       = $('#videoPane');

    const thumbBox        = $('#thumbBox');
    const thumbInput      = $('#thumb');
    const thumbPreview    = $('#thumbPreview');

    const videoUrl        = $('#videoUrl');
    const videoPreview    = $('#videoPreview');

    const metaRow         = $('#metaTitleIntro');
    const anchorImage     = $('#metaAnchorImage');
    const anchorVideo     = $('#metaAnchorVideo');

    const imagePane       = $('#imagePane'); // ← 조리 단계 카드

    const btnPublish      = $('#publish');
    const btnDraft        = $('#saveDraft');

    // === 비디오 ===
    function parseYouTube(url){
        try{
            const u = new URL((url||'').trim());
            if (u.hostname.includes('youtu.be'))  return u.pathname.slice(1);
            if (u.hostname.includes('youtube.com')) return u.searchParams.get('v');
        }catch(e){}
        return null;
    }
    function updateVideoPreview(){
        const id = parseYouTube(videoUrl?.value);
        if (id) videoPreview.src = `https://www.youtube.com/embed/${id}`;
        else    videoPreview.removeAttribute('src');
    }

    // === 탭/타입 전환 + 공용 필드 이동 ===
    function mountMeta(toType){
        if (toType === 'VIDEO') anchorVideo?.after(metaRow);
        else                    anchorImage?.after(metaRow);
    }
    function setRecipeType(type){
        const t = String(type).toUpperCase();
        const isImg = t === 'IMAGE';
        recipeTypeEl.value = isImg ? 'IMAGE' : 'VIDEO';

        tabImage?.classList.toggle('is-active', isImg);
        tabImage?.setAttribute('aria-selected', String(isImg));
        tabVideo?.classList.toggle('is-active', !isImg);
        tabVideo?.setAttribute('aria-selected', String(!isImg));

        thumbPane.style.display = isImg ? '' : 'none';
        imagePane.style.display = isImg ? '' : 'none';   // ← 추가
        videoPane.style.display = isImg ? 'none' : '';
        mountMeta(isImg ? 'IMAGE' : 'VIDEO');

        if (!isImg) updateVideoPreview();
        if (window.RecipesValidation?.revalidateTypeFields) {
               window.RecipesValidation.revalidateTypeFields();
             }
    }

    tabImage?.addEventListener('click', (e)=>{
        if (tabImage.disabled) return;
        e.preventDefault();
        setRecipeType('IMAGE');
    });
    tabVideo?.addEventListener('click', (e)=>{
        if (tabVideo.disabled) return;
        e.preventDefault();
        setRecipeType('VIDEO');
    });


    if (window.RecipesValidation?.revalidateTypeFields) {
        window.RecipesValidation.revalidateTypeFields();
        }

    // === 썸네일 미리보기 ===
    thumbInput?.addEventListener('change', (e)=>{
        const file = e.target.files?.[0];
        const ph = $('.thumb .ph');
        if (!file){
            thumbPreview?.classList.add('hidden');
            ph?.classList.remove('hidden');
            thumbPreview?.removeAttribute('src');
            return;
        }
        const url = URL.createObjectURL(file);
        thumbPreview.src = url;
        thumbPreview.classList.remove('hidden');
        ph?.classList.add('hidden');
        thumbPreview.onload = ()=> URL.revokeObjectURL(url);

         if (window.RecipesValidation?.revalidateTypeFields) {
             window.RecipesValidation.revalidateTypeFields();
             }
    });

    // === 태그 ===
    const tagInput = $('#tagInput');
    function syncTagHidden(){
        tagHidden.innerHTML = '';
        [...tagList.children].forEach((chip, i) => {
            const val = chip.dataset.tag;
            const hidden = document.createElement('input');
            hidden.type = 'hidden';
            hidden.name = `tags[${i}].tag`;
            hidden.value = val;
            tagHidden.appendChild(hidden);
        });
    }
    function addTag(text){
        const label = (text||'').trim().replace(/^#+/, '');
        if (!label) return;
        if ([...tagList.children].some(el => el.dataset.tag === label)) return;
        const el = document.createElement('span');
        el.className = 'tag';
        el.dataset.tag = label;
        el.innerHTML = `<span>#${label}</span><span class="x" title="삭제">×</span>`;
        tagList.appendChild(el);
        tagInput.value = '';
        syncTagHidden();
    }
    tagInput?.addEventListener('keydown', (e)=>{
        if (e.key === 'Enter'){ e.preventDefault(); addTag(tagInput.value); }
    });
    $('#addTagBtn')?.addEventListener('click', ()=> addTag(tagInput.value));
    tagList?.addEventListener('click', (e)=>{
        const x = e.target.closest('.x');
        if (!x) return;
        x.parentElement.remove();
        syncTagHidden();
    });

    // === 재료 ===
    function renumberIngredients(){
        $$('#ingredients .item-row').forEach((row, i) => {
            row.dataset.index = i;
            const inputs = row.querySelectorAll('input');
            const name = inputs[0];
            const amt  = inputs[1];
            let sort   = row.querySelector('input[type="hidden"]');
            if (!sort){
                sort = document.createElement('input');
                sort.type = 'hidden';
                row.appendChild(sort);
            }
            name.name = `ingredients[${i}].ingredientName`;
            amt.name  = `ingredients[${i}].ingredientAmount`;
            sort.name = `ingredients[${i}].sortOrder`;
            sort.value= String(i + 1);
        });
    }
    $('#addIng')?.addEventListener('click', ()=>{
        const row = document.createElement('div');
        row.className = 'item-row';
        row.innerHTML = `
      <input class="input" placeholder="재료명"/>
      <input class="input" placeholder="분량"/>
      <input type="hidden"/>
      <button type="button" class="btn icon del">🗑</button>`;
        ingredientsWrap.appendChild(row);
        renumberIngredients();
 //  여기서만 바인딩 (jQuery 필요)
        if ($jq && window.RecipesValidation?.rebindIngredients) {
        window.RecipesValidation.rebindIngredients($jq(row));
        }
    });
    ingredientsWrap?.addEventListener('click', (e)=>{
        if (e.target.closest('.del')){
            e.target.closest('.item-row')?.remove();
            if ($$('#ingredients .item-row').length === 0) $('#addIng').click();
            renumberIngredients();
        }

    });



    // === 단계 ===
    function updateMoveButtons(){
        const arr = $$('#steps .step');
        arr.forEach((step, i) => {
            const up  = step.querySelector('.move-up');
            const top = step.querySelector('.move-top');
            const dn  = step.querySelector('.move-down');
            const bt  = step.querySelector('.move-bottom');
            if (up)  up.disabled  = (i===0);
            if (top) top.disabled = (i===0);
            if (dn)  dn.disabled  = (i===arr.length-1);
            if (bt)  bt.disabled  = (i===arr.length-1);
        });
    }
    function makeStep(){
        const wrap = document.createElement('article');
        wrap.className = 'step';
        wrap.innerHTML = `
      <div class="step-head">
        <div class="step-title">Step <span class="no"></span></div>
        <div style="display:flex;gap:8px">
          <button type="button" class="btn icon move-top" title="맨 위로">⤒</button>
          <button type="button" class="btn icon move-up" title="위로">↑</button>
          <button type="button" class="btn icon move-down" title="아래로">↓</button>
          <button type="button" class="btn icon move-bottom" title="맨 아래로">⤓</button>
          <button type="button" class="btn icon add-below" title="아래에 단계 추가">＋</button>
          <button type="button" class="btn icon danger del" title="삭제">🗑</button>
        </div>
      </div>
      <div class="step-body">
        <label class="upload">
          <input type="file" name="stepImages" accept="image/*" />
          <span class="ph">이미지 업로드</span>
          <img class="hidden" alt="">
        </label>
        <textarea placeholder="설명(예: 팬에 올리브오일을 두르고 마늘을 볶습니다.)"></textarea>
        <input type="hidden" class="fld-order" value="0"/>
        <input type="hidden" class="fld-id"    value=""/>
      </div>`;
        return wrap;
    }
    function renumberSteps(){
        $$('#steps .step').forEach((step, i) => {
            step.dataset.index = i;
            const no = step.querySelector('.no');
            if (no) no.textContent = String(i + 1);

            const ta = step.querySelector('textarea');
            if (ta) ta.name = `contents[${i}].stepExplain`;

            let order = step.querySelector('.fld-order');
            if (!order){
                order = document.createElement('input');
                order.type = 'hidden';
                order.className = 'fld-order';
                step.querySelector('.step-body')?.appendChild(order);
            }
            order.name  = `contents[${i}].stepOrder`;
            order.value = String(i + 1);

            const id = step.querySelector('.fld-id');
            if (id) id.name = `contents[${i}].stepId`;

            // 파일 input은 동일 name으로(서버가 순서대로 받게)
            const file = step.querySelector('input[type="file"]');
            if (file) file.name = 'stepImages';
        });
        updateMoveButtons();


    }

    steps?.addEventListener('click', (e)=>{
        const step = e.target.closest('.step');
        if (!step) return;

        if (e.target.closest('.del')){
            step.remove();
            if ($$('#steps .step').length === 0) steps.appendChild(makeStep());
            renumberSteps();
            return;
        }
        if (e.target.closest('.add-below')){
            const newStep = makeStep();
            steps.insertBefore(newStep, step.nextElementSibling);
            renumberSteps();
            // ✅ 여기서만 바인딩 (jQuery 필요)
             if ($jq && window.RecipesValidation?.rebindSteps) {
               window.RecipesValidation.rebindSteps($jq(newStep));
             }

            newStep.scrollIntoView({behavior:'smooth', block:'center'});
            return;
        }
        if (e.target.closest('.move-up')){
            const prev = step.previousElementSibling;
            if (prev) steps.insertBefore(step, prev);
            renumberSteps();
            return;
        }
        if (e.target.closest('.move-down')){
            const next = step.nextElementSibling;
            if (next) steps.insertBefore(next, step);
            renumberSteps();
            return;
        }
        if (e.target.closest('.move-top')){
            const first = steps.firstElementChild;
            if (first && first!==step) steps.insertBefore(step, first);
            renumberSteps();
            return;
        }
        if (e.target.closest('.move-bottom')){
            steps.appendChild(step);
            renumberSteps();
            return;
        }
    });

    // 단계 이미지 미리보기
    steps?.addEventListener('change', (e)=>{
        const fileInput = e.target.matches('input[type="file"]') ? e.target : e.target.closest('.upload')?.querySelector('input[type="file"]');
        if (!fileInput) return;
        const label = fileInput.closest('.upload');
        const img = label.querySelector('img');
        const ph = label.querySelector('.ph');
        const file = fileInput.files && fileInput.files[0];
        if (!file){
            img.classList.add('hidden'); img.removeAttribute('src'); ph.classList.remove('hidden'); return;
        }
        const url = URL.createObjectURL(file);
        img.src = url; img.classList.remove('hidden'); ph.classList.add('hidden');
        img.onload = () => URL.revokeObjectURL(url);
    });

    // === 비디오 입력 미리보기 ===
    videoUrl?.addEventListener('input', ()=>{
        updateVideoPreview();
        if (window.RecipesValidation?.revalidateTypeFields) {
               window.RecipesValidation.revalidateTypeFields();
             }
    } );

    // === 제출 직전, 중복 필드 제거 & 덤프 ===
    function sanitizeAndDebugForm() {
        if (!form) return;

        // 1) steps 컨테이너 밖에 있는 contents[...] 전부 제거 (유령 히든 방지)
        form.querySelectorAll('input[name^="contents["], textarea[name^="contents["]').forEach(el=>{
            if (!steps.contains(el)) {
                console.warn('[recipe] removed stray field outside #steps:', el.name);
                el.remove();
            }
        });

        // 2) 각 step 내부에서 기대하는 name만 남기고 다 제거
        $$('#steps .step').forEach((step, i) => {
            const expected = new Set([
                `contents[${i}].stepExplain`,
                `contents[${i}].stepOrder`,
                `contents[${i}].stepId`
            ]);
            step.querySelectorAll('input[name^="contents["], textarea[name^="contents["]').forEach(el=>{
                if (!expected.has(el.name)) {
                    console.warn('[recipe] removed unexpected field:', el.name);
                    el.remove();
                }
            });
        });

        // 3) 덤프: 같은 name이 몇 개나 있는지 콘솔에서 바로 확인
        const fd = new FormData(form);
        const counts = {};
        for (const [k] of fd.entries()) counts[k] = (counts[k] || 0) + 1;
        console.table(Object.entries(counts)
            .filter(([k]) => k.startsWith('contents['))
            .map(([k,c]) => ({ name:k, count:c })));
    }

    // === 제출 락 & 공통 제출 ===
    let __submitting = false;
    function disableOnce(btn){
        if (!btn) return;
        btn.disabled = true;
        setTimeout(()=> btn.disabled = false, 3000);
    }
    function finalizeAndSubmit(){
        if (__submitting) return;
        __submitting = true;
        setTimeout(()=>{ __submitting = false; }, 3000);

        if (!form){ console.error('[recipe] form not found'); return; }
        if (typeof renumberIngredients === 'function') renumberIngredients();
        if (typeof renumberSteps === 'function') renumberSteps();
        if (typeof syncTagHidden === 'function') syncTagHidden();

        // 2) 중복 필드 제거 + 디버그 덤프
        sanitizeAndDebugForm();

        if (typeof form.requestSubmit === 'function') form.requestSubmit();
        else form.submit();
    }

    // TODO: 디버깅
    // const fd = new FormData(document.querySelector('#recipeForm'));
    // console.log('intro=', fd.get('recipeIntro'));
    // console.log('title=', fd.get('recipeTitle'));
    // console.log([...fd.entries()].filter(([k])=>k.startsWith('ingredients[')));


    btnPublish?.addEventListener('click', (e)=>{
        disableOnce(e.currentTarget);
        postStatus.value = isPublic?.checked ? 'PUBLIC' : 'PRIVATE';
        finalizeAndSubmit();
    });
    btnDraft?.addEventListener('click', (e)=>{
        disableOnce(e.currentTarget);
        postStatus.value = 'DRAFT';
        finalizeAndSubmit();
    });

    // === 초기화 ===
    document.addEventListener('DOMContentLoaded', ()=>{
        // 타입/탭/메타 위치
        setRecipeType(recipeTypeEl?.value || 'IMAGE');
        // 비디오 프리뷰(기존 값 있으면)
        updateVideoPreview?.();
        // 번호/인덱스 보정
        renumberIngredients?.();
        renumberSteps?.();
        // 태그 히든 보정
        syncTagHidden?.();
    });
})();

