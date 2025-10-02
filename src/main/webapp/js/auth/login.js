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

// 제출
$('#loginForm').addEventListener('submit', (e)=>{
    e.preventDefault();
    const payload = {
        email: $('#email').value.trim(),
        password: $('#pw').value,
    };
    e.target.submit();
});