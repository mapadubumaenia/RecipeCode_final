<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile</title>

    <!-- 공통 CSS -->
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile_feed.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/notification.css'/>">

</head>
<body>
<main class="container profile-page">

    <!-- 헤더 -->
    <header class="container">
        <div class="flex-box">
            <div class="flex-row">
                <h1 class="page-title">Profile</h1>
                <a href="${pageContext.request.contextPath}/" class="float-text">home</a>
            </div>

            <!-- ▶ 추가: 알림 + 마이페이지 -->
            <div class="header-actions">
                <sec:authorize access="isAuthenticated()">
                    <sec:authentication property="principal" var="loginUser"/>
                    <a class="alink"  href="${pageContext.request.contextPath}/mypage">${loginUser.nickname}님</a>
                </sec:authorize>

                <div class="notif-wrap">
                    <!-- 알림 버튼 -->
                    <button
                            id="btnNotif"
                            class="notif-btn"
                            aria-haspopup="dialog"
                            aria-expanded="false"
                            aria-controls="notifPanel"
                            title="알림"
                    >
                        🔔
                        <span class="notif-dot" aria-hidden="true"></span>
                    </button>

                    <!-- 드롭다운 패널 -->
                    <div
                            id="notifPanel"
                            class="notif-panel"
                            role="dialog"
                            aria-label="알림 목록"
                    >
                        <div class="notif-head">
                            <strong>알림</strong>
                            <div class="actions">
                                <button class="btn small ghost" id="markAll">모두 읽음</button>
                            </div>
                        </div>

                        <div class="notif-list" id="notifList"><!-- JS 렌더 --></div>

                        <div class="notif-foot">
                            <button class="btn small ghost" id="closeNotif">닫기</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </header>

    <!-- 프로필 카드 -->
    <section class="card profile-card">
        <aside class="left">
        <div class="cardLeft">
            <div class="profile-img">
            <!-- 프로필 이미지 -->
            <div class="avatar-lg">
                <c:if test="${not empty user.profileImageUrl}">
                    <img src="${user.profileImageUrl}" alt="${user.nickname}" class="avatar-lg"/>
                </c:if>
            </div>
        </div>

        <!-- 프로필 정보 -->
        <div class="profile-info">
            <!-- 아이디 + 닉네임 -->
            <div class="profile-top">
                <div class="userInfo">
                    <div class="profile">
                        <h2 class="profile-name">${user.userId}</h2>
                    <span class="muted">${user.nickname}</span>
                    </div>
                    <div class="profile-left">
                        <!-- 위치 -->
                        <c:if test="${not empty user.userLocation}">
                            <p class="muted">📍 ${user.userLocation}</p>
                        </c:if>
                    </div>
                </div>
            </div>
            </div>
        </div>
        </aside>


            <!-- 짧은 소개 -->
<%--            <c:if test="${not empty user.userIntroduce}">--%>
<%--                <p class="intro">${user.userIntroduce}</p>--%>
<%--            </c:if>--%>

            <!-- 위치 -->
<%--            <c:if test="${not empty user.userLocation}">--%>
<%--                <p class="muted">📍 ${user.userLocation}</p>--%>
<%--            </c:if>--%>

            <!-- 관심 태그 -->
<%--            <c:if test="${not empty user.interestTags}">--%>
<%--                <div class="tags">--%>
<%--                    <c:forEach items="${user.interestTags}" var="t">--%>
<%--                        <span class="chip">#${t.tag}</span>--%>
<%--                    </c:forEach>--%>
<%--                </div>--%>
<%--            </c:if>--%>

            <!-- SNS / 링크 아이콘 -->
<%--            <div class="profile-links">--%>
<%--                <c:if test="${not empty user.userWebsite}">--%>
<%--                    <a href="${user.userWebsite}" target="_blank" class="link-icon">🌐</a>--%>
<%--                </c:if>--%>
<%--                <c:if test="${not empty user.userInsta}">--%>
<%--                    <a href="https://instagram.com/${user.userInsta}" target="_blank" class="link-icon">📸</a>--%>
<%--                </c:if>--%>
<%--                <c:if test="${not empty user.userYoutube}">--%>
<%--                    <a href="https://youtube.com/${user.userYoutube}" target="_blank" class="link-icon">▶</a>--%>
<%--                </c:if>--%>
<%--                <c:if test="${not empty user.userBlog}">--%>
<%--                    <a href="${user.userBlog}" target="_blank" class="link-icon">✍</a>--%>
<%--                </c:if>--%>
<%--            </div>--%>


        <!-- SNS / 링크 아이콘 -->
        <aside class="card-bottom">
            <!-- 팔로워 / 팔로잉 -->
            <div class="profile-right">
                <div class="profile-stats">
                    <a href="/follow/network/${user.userId}" class="stat-link">
                        <span class="f-text">Followers</span> <span class="fc-text" id="followerCount">0</span>
                    </a>
                    <a href="/follow/network/${user.userId}" class="stat-link">
                        <span class="f-text">Following</span> <span class="fc-text" id="followingCount">0</span>
                    </a>
                </div>
                <div class="user-intro">
                    <!-- 짧은 소개 -->
                    <c:if test="${not empty user.userIntroduce}">
                        <p class="intro">${user.userIntroduce}</p>
                    </c:if>
                </div>
            </div>

            <div class="bottom-left">
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
            <!-- 관심 태그 -->
            <c:if test="${not empty user.interestTags}">
                <div class="tags">
                    <c:forEach items="${user.interestTags}" var="t">
                        <span class="chip">#${t.tag}</span>
                    </c:forEach>
                </div>
            </c:if>
            </div>
        </aside>

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
            <h2 class="section-title">Network</h2>

            <!-- 탭 버튼 -->
            <div class="follow-tabs">
                <button class="tab-btn is-active" data-tab="followers">Follower</button>
                <button class="tab-btn" data-tab="following">Following</button>
            </div>

            <!-- Followers 목록 -->
            <div id="followersList" class="follow-list">
                <c:forEach var="f" items="${followers}" varStatus="st">
                    <c:if test="${st.index < 5}">
                        <div class="mini-card" data-userid="${f.member.userId}">
                            <!-- 왼쪽: 프로필 이미지 -->
                            <c:if test="${empty f.member.profileImageUrl}">
                                <img src="/images/default_profile.jpg" alt="기본 이미지">
                            </c:if>
                            <c:if test="${not empty f.member.profileImageUrl}">
                                <img src="${f.member.profileImageUrl}" alt="${f.member.nickname}">
                            </c:if>

                            <!-- 중앙: 유저 정보 -->
                            <div class="mini-info">
                                <div class="mini-top">
                                    <a href="/follow/network/${f.member.userId}"><span class="user-id">${f.member.userId}</span></a>
                                    <span class="muted">${f.member.nickname}</span>
                                    <div class="mini-stats">
                                        <span class="f-count">팔로워 <b><c:out value="${followerCounts[f.member.userId]}" default="0"/></b></span>
                                        <span class="f-count">팔로잉 <b><c:out value="${followingCounts[f.member.userId]}" default="0"/></b></span>
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
                    <a href="/follow/network/${user.userId}" class="btn small ghost">더보기</a>
                </c:if>
            </div>

            <!-- Following 목록 -->
            <div id="followingList" class="follow-list hidden">
                <c:forEach var="f" items="${followings}" varStatus="st">
                    <c:if test="${st.index < 5}">
                        <div class="mini-card" data-userid="${f.member.userId}">
                            <!-- 왼쪽: 프로필 이미지 -->
                            <img src="${f.member.profileImageUrl}" alt="">

                            <!-- 중앙: 유저 정보 -->
                            <div class="mini-info">
                                <div class="mini-top">
                                    <a href="/follow/network/${f.member.userId}"><span class="user-id">${f.member.userId}</span></a>
                                    <span class="muted">${f.member.nickname}</span>
                                    <div class="mini-stats">
                                        <span class="f-count">팔로워 <b><c:out value="${followerCounts[f.member.userId]}" default="0"/></b></span>
                                        <span class="f-count">팔로잉 <b><c:out value="${followingCounts[f.member.userId]}" default="0"/></b></span>
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
                    <a href="/follow/network/${user.userId}" class="btn small ghost">더보기</a>
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

<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<!-- 무한 스크롤 기타 기능 JS -->
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<script src="<c:url value='/js/mypage/profile-feed.js'/>"></script>
<%--알림 js--%>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
</body>
</html>
