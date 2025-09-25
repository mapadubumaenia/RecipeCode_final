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
        <a class="btn-logout" href="/auth/login">Back</a>
    </header>

    <section class="auth">
        <article class="card p-16">
            <!-- 이메일 -->
            <form method="post" action="">
                <div class="field">
                    <label for="email">가입 이메일</label>
                    <input class="input" type="email" id="email" name="email" placeholder="you@example.com" required>
                </div>
                <div class="actions">
                    <button class="btn primary" type="submit">인증코드 발송</button>
                </div>
            </form>
        </article>

        <article class="card p-16">
            <!-- 인증코드 + 새 비밀번호 -->
            <form method="post" action="">
                <div class="field">
                    <label for="code">인증코드</label>
                    <input class="input" type="text" id="code" name="code" required>
                </div>
                <div class="field">
                    <label for="newPw">새 비밀번호</label>
                    <input class="input" type="password" id="newPw" name="newPassword" required>
                </div>
                <div class="field">
                    <label for="newPwConfirm">비밀번호 확인</label>
                    <input class="input" type="password" id="newPwConfirm" name="newPasswordConfirm" required>
                </div>
                <div class="actions">
                    <button class="btn primary" type="submit">비밀번호 재설정</button>
                </div>
            </form>
        </article>
    </section>
</main>
</body>
</html>