<%--
  Created by IntelliJ IDEA.
  User: member
  Date: 25. 9. 10.
  Time: 오전 11:54
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>Sign Up</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/register_page.css'/>">
</head>
<body>
<main class="container">

    <!-- 헤더 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">Sign Up</h1>
            <a href="/home" class="float-text">home</a>
        </div>
        <a class="btn-back" href="/home">Back</a>
    </header>

    <!-- 가입 카드 -->
    <section class="auth">
        <article class="card p-16">
            <form id="signupForm" novalidate action="/auth/register/addition" method="post">
                <%-- TODO: csrf 인증 토큰(중요): 안하면 로그인페이지로 redirect 됨 --%>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <!-- 이메일 -->
                <div class="field">
                    <label for="email">이메일</label>
                    <input class="input" type="email" id="email" name="userEmail" placeholder="you@example.com" required />
                    <div class="hint">로그인 및 알림용. 공개되지 않습니다.</div>
                </div>

                <!-- 아이디(핸들) -->
                <div class="field">
                    <label for="handle">아이디</label>
                    <div class="input-wrap">
                        <input class="input" type="text" id="handle" name="userId" placeholder="yourid" minlength="3" maxlength="20" required />
                    </div>
                    <div class="hint" id="handleHint">영문/숫자/밑줄, 3–20자</div>
                </div>

                <!-- 표시 이름 -->
                <div class="field">
                    <label for="display">닉네임</label>
                    <input class="input" type="text" id="display" name="nickname" placeholder="예) Chef Riddle" maxlength="40" />
                </div>

                <!-- 비밀번호 -->
                <div class="field">
                    <label for="pw">비밀번호</label>
                    <div class="input-wrap">
                        <input class="input" type="password" id="pw" name="password" placeholder="8자 이상, 대/소문자+숫자 조합 권장" minlength="8" required />
                        <button class="toggle" type="button" aria-label="비밀번호 표시" data-target="#pw">👁️</button>
                    </div>
                    <div class="strength" aria-hidden="true"><span id="pwBar"></span></div>
                </div>

                <!-- 비밀번호 확인 -->
                <div class="field">
                    <label for="pw2">비밀번호 확인</label>
                    <div class="input-wrap">
                        <input class="input" type="password" id="pw2" placeholder="다시 입력" required />
                        <button class="toggle" type="button" aria-label="비밀번호 표시" data-target="#pw2">👁️</button>
                    </div>
                    <div class="hint" id="pwMatchHint"></div>
                </div>

                <!-- 약관 동의 -->
                <div class="field">
                    <label class="switch">
                        <input type="checkbox" id="agree" />
                        <span>이용 약관 및 개인정보 처리방침에 동의합니다.</span>
                    </label>
                </div>

                <!-- 소셜 (옵션, 와이어프레임용) -->
                <div class="divider">또는</div>
                <div class="social">
                    <button type="button" class="btn social" onclick="location.href='<c:url value='/oauth2/authorization/google'/>'">🔵 Continue with Google</button>
                    <button type="button" class="btn social" onclick="location.href='<c:url value='/oauth2/authorization/kakao'/>'">🟡 Continue with Kakao</button>
                </div>

                <!-- 제출 -->
                <div class="actions" style="margin-top:12px">
                    <div class="left">
                        <a class="btn primary" href="/auth/login">로그인으로 이동</a>
                    </div>
                    <button class="btn primary" id="submitBtn" type="submit" disabled>가입하기</button>
                </div>

                <div class="error" id="formError" aria-live="polite"></div>
            </form>
        </article>
    </section>
</main>

<script>
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
        console.log('SIGNUP PAYLOAD', payload);
        alert('가입 폼 검증 완료! (백엔드 연동 지점)');
    });
</script>
</body>
</html>