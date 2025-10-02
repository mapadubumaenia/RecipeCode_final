// ===== 유틸 =====
const $ = (s, el=document) => el.querySelector(s);
const $$ = (s, el=document) => [...el.querySelectorAll(s)];

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
const pwBar = $('#pwBar');
const submitBtn = $('#submitBtn');
const agree = $('#agree');
const formError = $('#formError');
const handle = $('#handle');
const handleHint = $('#handleHint');

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

function validateHandle(){
    const v = handle.value.trim();
    const ok = /^[A-Za-z0-9_]{3,20}$/.test(v.replace(/^@/,''));
    handleHint.textContent = ok ? '사용 가능한 형식입니다.' : '영문/숫자/밑줄만, 3–20자';
    handleHint.style.color = ok ? '#16a34a' : 'var(--muted)';
    return ok;
}

function validateAll(){
    const emailOk = $('#email').checkValidity();
    const handleOk = validateHandle();
    const pwOk = pw.value.length >= 8;
    const matchOk = pw.value && pw.value === pw2.value;
    const agreeOk = agree.checked;
    const allOk = emailOk && handleOk && pwOk && matchOk && agreeOk;
    submitBtn.disabled = !allOk;
    formError.textContent = allOk ? '' : '';
}

['input','change'].forEach(ev=>{
    pw.addEventListener(ev, ()=>{ renderPwBar(); validateAll(); });
    pw2.addEventListener(ev, ()=>{ renderPwMatch(); validateAll(); });
    handle.addEventListener(ev, ()=>{ validateHandle(); validateAll(); });
    $('#email').addEventListener(ev, validateAll);
    agree.addEventListener(ev, validateAll);
});

// 초기 상태
renderPwBar(); validateHandle(); validateAll();

// 제출 (와이어프레임용)
$('#signupForm').addEventListener('submit', (e)=>{
    e.preventDefault();
    if(submitBtn.disabled) return;
    const payload = {
        email: $('#email').value.trim(),
        handle: handle.value.trim().replace(/^@/,''),
        displayName: $('#display').value.trim(),
        password: pw.value, // 실제 서비스에서는 해시/전송 보호
        agree: agree.checked
    };
    e.target.submit();
});