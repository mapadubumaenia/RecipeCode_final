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

        <!-- 프로필 이미지 -->
        <div class="avatar-lg">
            <c:if test="${not empty user.profileImageUrl}">
                <img src="${user.profileImageUrl}" alt="${user.nickname}" class="avatar-lg"/>
            </c:if>
        </div>

        <!-- 프로필 정보 -->
        <div class="profile-info">

            <!-- 아이디 + 닉네임 -->
            <div class="profile-top">
                <h2 class="profile-name">${user.userId}</h2>
                <span class="muted">${user.nickname}</span>
            </div>

            <!-- 팔로워 / 팔로잉 -->
            <div class="profile-stats">
                <span>팔로워 <b id="followerCount">0</b></span>
                <span>팔로잉 <b id="followingCount">0</b></span>
            </div>

            <!-- 짧은 소개 -->
            <c:if test="${not empty user.userIntroduce}">
                <p class="intro">${user.userIntroduce}</p>
            </c:if>

            <!-- 위치 -->
            <c:if test="${not empty user.userLocation}">
                <p class="muted">📍 ${user.userLocation}</p>
            </c:if>

            <!-- 관심 태그 -->
            <c:if test="${not empty user.interestTags}">
                <div class="tags">
                    <c:forTokens items="${user.interestTags}" delims="," var="tag">
                        <span class="chip">#${tag}</span>
                    </c:forTokens>
                </div>
            </c:if>

            <!-- SNS / 링크 아이콘 -->
            <div class="profile-links">
                <c:if test="${not empty user.userWebsite}">
                    <a href="${user.userWebsite}" target="_blank" class="link-icon">🌐</a>
                </c:if>
                <c:if test="${not empty user.userInsta}">
                    <a href="https://instagram.com/${user.userInsta}" target="_blank" class="link-icon">📸</a>
                </c:if>
                <c:if test="${not empty user.userYoutube}">
                    <a href="https://youtube.com/${user.userYoutube}" target="_blank" class="link-icon">▶</a>
                </c:if>
                <c:if test="${not empty user.userBlog}">
                    <a href="${user.userBlog}" target="_blank" class="link-icon">✍</a>
                </c:if>
            </div>
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
            <div id="feedContainer" data-user="${user.userId}"></div>
        </aside>

        <!-- 사이드바 -->
        <aside class="feed-list sidebar">
            <h2 class="section-title">Connections</h2>

            <!-- 탭 버튼 -->
            <div class="follow-tabs">
                <button class="tab-btn is-active" data-tab="follower">Follower</button>
                <button class="tab-btn" data-tab="followers">Following</button>
            </div>

            <!-- Followers 목록 -->
            <div id="followersList" class="follow-list hidden">
                <c:forEach var="f" items="${followers}" varStatus="st">
                    <c:if test="${st.index < 10}">
                        <div class="mini-card" data-userid="${f.member.userId}">
                            <!-- 왼쪽: 프로필 이미지 -->
                            <img src="${f.member.profileImageUrl}" alt="">

                            <!-- 중앙: 유저 정보 -->
                            <div class="mini-info">
                                <div class="mini-top">
                                    <span class="user-id">${f.member.userId}</span>
                                    <span class="muted">${f.member.nickname}</span>
                                    <div class="mini-stats">
                                        <span class="f-count">팔로워 <b>0</b></span>
                                        <span class="f-count">팔로잉 <b>0</b></span>
                                    </div>
                                </div>
                                <c:if test="${not empty f.member.userLocation}">
                                    <span class="muted">📍 ${f.member.userLocation}</span>
                                </c:if>
                            </div>

                            <!-- 오른쪽: 향후 버튼 자리 -->
                            <div class="mini-action"></div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${followersHasNext}">
                    <button class="btn small ghost">더보기</button>
                </c:if>
            </div>

            <!-- Following 목록 -->
            <div id="followingList" class="follow-list">
                <c:forEach var="f" items="${followings}" varStatus="st">
                    <c:if test="${st.index < 10}">
                        <div class="mini-card" data-userid="${f.member.userId}">
                            <!-- 왼쪽: 프로필 이미지 -->
                            <img src="${f.member.profileImageUrl}" alt="">

                            <!-- 중앙: 유저 정보 -->
                            <div class="mini-info">
                                <div class="mini-top">
                                    <span class="user-id">${f.member.userId}</span>
                                    <span class="muted">${f.member.nickname}</span>
                                    <div class="mini-stats">
                                        <span class="f-count">팔로워 <b>0</b></span>
                                        <span class="f-count">팔로잉 <b>0</b></span>
                                    </div>
                                </div>
                                <c:if test="${not empty f.member.userLocation}">
                                    <span class="muted">📍 ${f.member.userLocation}</span>
                                </c:if>
                            </div>

                            <!-- 오른쪽: 향후 버튼 자리 -->
                            <div class="mini-action"></div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${followingsHasNext}">
                    <button class="btn small ghost">더보기</button>
                </c:if>
            </div>

        </aside>
    </section>
</main>

<script>
    // 프로필 주인 (지금 보고 있는 페이지의 대상 유저)
    const profileUserId = "${user.userId}";

    // 현재 로그인한 사용자
    const currentUserEmail = "${pageContext.request.userPrincipal.name}";
</script>


<!-- JS -->
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<script src="<c:url value='/js/mypage/profile-feed.js'/>"></script>
</body>
</html>
