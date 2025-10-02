<%--
  Created by IntelliJ IDEA.
  User: member
  Date: 25. 9. 10.
  Time: 오전 11:54
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>Login</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/register_page.css'/>">
</head>
<body>
<main class="container">
    <!-- 헤더 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">Login</h1>
            <a href="/home" class="float-text">home</a>
        </div>
        <a class="btn-back" href="/auth/register">Back</a>
    </header>

    <!-- 로그인 카드 -->
    <section class="auth">
        <article class="card p-16">
            <!-- 나중에 JSP form으로 교체 -->
            <form id="loginForm" method="post" action="/auth/loginProcess">
                <%-- TODO: csrf 인증 토큰(중요): 안하면 로그인페이지로 redirect 됨 --%>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <!-- 이메일 -->
                <div class="field">
                    <label for="email">이메일</label>
                    <input class="input" type="email" id="email" name="userEmail" placeholder="you@example.com" required />
                </div>

                <!-- 비밀번호 -->
                <div class="field">
                    <label for="pw">비밀번호</label>
                    <div class="input-wrap">
                        <input class="input" type="password" id="pw" name="password" placeholder="비밀번호 입력" minlength="8" required />
                        <button class="toggle" type="button" aria-label="비밀번호 표시" data-target="#pw">👁️</button>
                    </div>
                </div>

                <!-- 소셜 로그인 -->
                <div class="divider">또는</div>
                <div class="social">


                    <button type="button" class="btn social" onclick="location.href='<c:url value='/oauth2/authorization/google'/>'"><img src="<c:url value='/images/google_logo.png'/>"
                                                                                                                                              alt="Google" style="width:12px; height:12px; vertical-align:middle;">Continue with Google</button>
                    <button type="button" class="btn social" onclick="location.href='<c:url value='/oauth2/authorization/kakao'/>'"><img src="<c:url value='/images/kakao_logo.png'/>"
                                                                                                                                          alt="Kakao" style="width:16px; height:16px; vertical-align:middle;">Continue with Kakao</button>
                </div>

                <!-- 제출 -->
                <div class="actions" style="margin-top:12px">
                    <div class="left">
                        <a class="btn primary" href="/auth/register">회원가입</a>
                        <a class="btn primary" href="/auth/findPassword">비밀번호 찾기</a>
                    </div>
                    <button class="btn primary" id="submitBtn" type="submit">로그인</button>
                </div>

                <div class="error" id="formError" aria-live="polite"></div>
            </form>
        </article>
    </section>
</main>
<%--  JS  --%>
<script src="<c:url value='/js/auth/login.js'/>"></script>
</body>
</html>