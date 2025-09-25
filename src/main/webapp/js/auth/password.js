// 비밀번호 보기 토글
$$(".toggle").forEach(btn=>{
    btn.addEventListener('click', ()=>{
        const input = document.querySelector(btn.dataset.target);
        if(!input) return;
        input.type = input.type === 'password' ? 'text' : 'password';
    });
});

// 비밀번호 강도 (아주 단순화)
const pw = $('#pw');
const pw2 = $('#pw2');
const formError = $('#formError');
const submitBtn = $('#savebtn');
const pwBar = $('#pwBar');

function scorePassword(v){
    let s = 0;
    if(v.length >= 8) s += 30;
    if(/[A-Z]/.test(v)) s += 20;
    if(/[a-z]/.test(v)) s += 20;
    if(/[0-9]/.test(v)) s += 20;
    if(/[^A-Za-z0-9]/.test(v)) s += 10;
    return Math.min(s, 100);
}

function renderPwBar(){
    const sc = scorePassword(pw.value);
    pwBar.style.width = sc + '%';
    pwBar.style.background = sc >= 70 ? '#16a34a' : (sc >= 40 ? '#d97706' : '#dc2626');
}

function renderPwMatch(){
    const hint = $('#pwMatchHint');
    if(!pw2.value){ hint.textContent = ''; return; }
    hint.textContent = pw.value === pw2.value ? '비밀번호가 일치합니다.' : '비밀번호가 일치하지 않습니다.';
    hint.style.color = pw.value === pw2.value ? '#16a34a' : '#dc2626';
}


function validateAll(){
    const hasPw = pw.value.length > 0;
    const pwOk = !hasPw || pw.value.length >= 8;
    const matchOk =!hasPw || (pw.value === pw2.value);
    const allOk = pwOk && matchOk
    submitBtn.disabled = !allOk;
    formError.textContent = allOk ? '' : '비밀번호가 8자리 미만이거나 일치하지 않습니다.';
}

// 초기 상태
validateAll(); renderPwBar();

['input','change'].forEach(ev=>{
    pw.addEventListener(ev, ()=>{ renderPwBar(); validateAll(); });
    pw2.addEventListener(ev, ()=>{ renderPwMatch(); validateAll(); });
});