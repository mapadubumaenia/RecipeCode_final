<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile</title>

    <!-- 공통 CSS -->
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile_feed.css'/>">
</head>
<body>
<main class="container profile-page">

    <!-- 헤더 -->
    <header class="container">
        <div class="flex-box">
            <div class="flex-row">
                <h1 class="page-title">Profile</h1>
                <a href="${pageContext.request.contextPath}/feed/main" class="float-text">home</a>
            </div>

            <!-- 알림 + 로그아웃 -->
            <div class="header-actions">
                <form action="${pageContext.request.contextPath}/logout" method="post">
                    <button type="submit" class="btn-logout">logout</button>
                </form>
                <div class="notif-wrap">
                    <button id="btnNotif" class="notif-btn" title="알림">🔔</button>
                </div>
            </div>
        </div>
    </header>

    <!-- 프로필 카드 -->
    <section class="card profile-card">
        <div class="avatar-lg">
            <c:if test="${not empty user.profileImageUrl}">
                <img src="${user.profileImageUrl}" alt="${user.nickname}" class="avatar-lg"/>
            </c:if>
        </div>
        <div class="profile-info">
            <div class="profile-top">
                <h2 class="profile-name">@${user.userId}</h2>
            </div>
            <p class="muted">${user.userIntroduce}</p>
        </div>
    </section>

    <!-- 메인 레이아웃 -->
    <section class="container layout">
        <!-- 유저 포스트 (무한 스크롤) -->
        <aside id="posts" class="feed-list">
            <nav class="tabs">
                <button class="tab is-active">Feed</button>
            </nav>

            <!-- JS에서 불러와서 채워질 컨테이너 -->
            <div id="feedContainer" data-user="${user.userEmail}"></div>
        </aside>

        <!-- 사이드바: 팔로워/팔로잉 미리보기 -->
        <aside class="feed-list sidebar">
            <h2 class="section-title m-0">Followers</h2>
            <c:forEach var="f" items="${followers}">
                <div class="mini-card">
                    <c:if test="${not empty f.member.profileImageUrl}">
                        <img src="${f.member.profileImageUrl}" alt="">
                    </c:if>
                    <span>@${f.member.nickname}</span>
                </div>
            </c:forEach>
            <c:if test="${followersHasNext}">
                <a href="${pageContext.request.contextPath}/follow/${user.userEmail}/follower"
                   class="btn small ghost">더보기</a>
            </c:if>

            <h2 class="section-title m-0">Following</h2>
            <c:forEach var="f" items="${followings}">
                <div class="mini-card">
                    <c:if test="${not empty f.member.profileImageUrl}">
                        <img src="${f.member.profileImageUrl}" alt="">
                    </c:if>
                    <span>@${f.member.nickname}</span>
                </div>
            </c:forEach>
            <c:if test="${followingsHasNext}">
                <a href="${pageContext.request.contextPath}/follow/${user.userEmail}/following"
                   class="btn small ghost">더보기</a>
            </c:if>
        </aside>
    </section>
</main>

<!-- JS 로딩 -->
<script src="<c:url value='/js/mypage/profile-feed.js'/>"></script>
</body>
</html>
