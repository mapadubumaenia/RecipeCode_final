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
                    <button type="button" class="btn social googlebtn" onclick="location.href='<c:url value='/oauth2/authorization/google'/>'"><img src="<c:url value='/images/google_logo.png'/>"
                                                                                                                                          alt="Google" style="width:12px; height:12px; vertical-align:middle;">Continue with Google</button>
                    <button type="button" class="btn social kakaobtn" onclick="location.href='<c:url value='/oauth2/authorization/kakao'/>'"><img src="<c:url value='/images/kakao_logo.png'/>"
                                                                                                                                         alt="Kakao" style="width:16px; height:16px; vertical-align:middle;">Continue with Kakao</button>
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
<%--  JS  --%>
<script src="<c:url value='/js/auth/register.js'/>"></script>
</body>
</html>