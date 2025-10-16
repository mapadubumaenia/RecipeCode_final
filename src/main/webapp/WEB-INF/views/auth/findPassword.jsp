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
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>비밀번호 찾기</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/register_page.css'/>">
</head>
<body>
<main class="container">
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">비밀번호 찾기</h1>
            <a href="/home" class="float-text">home</a>
        </div>
        <a class="btn-back" href="/auth/login">Back</a>
    </header>

    <section class="auth">
        <article class="card p-16">
            <!-- 이메일 -->
            <form method="post" action="">
                <!-- ✅ CSRF 토큰 추가 -->
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="field">
                    <label for="email">가입 이메일</label>
                    <input class="input" type="email" id="email" name="email" placeholder="you@example.com" required>
                </div>
                <div class="actions">
                    <button class="btn primary" id="codebtn" type="submit">인증코드 발송</button>
                </div>
            </form>
        </article>

        <article class="card p-16">
            <!-- 인증코드 + 새 비밀번호 -->
            <form method="post" action="">
                <!-- ✅ CSRF 토큰 추가 -->
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="field">
                    <label for="code">인증코드</label>
                    <div class="input-wrap">
                        <input class="input" type="text" id="code" name="code" required>
                        <div class="timer" id="timer"></div>
                    </div>
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
                    <div class="error" id="formError" aria-live="polite"></div>
                </div>

                <div class="actions">
                    <button class="btn primary" id="savebtn" type="submit">비밀번호 재설정</button>
                </div>
            </form>
        </article>
    </section>
</main>
<%--  JS  --%>
<script src="<c:url value='/js/auth/findPassword.js'/>"></script>
<script src="<c:url value='/js/auth/password.js'/>"></script>
</body>
</html>