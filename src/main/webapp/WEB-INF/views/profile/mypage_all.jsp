<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 16.
  Time: 오후 5:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile</title>

    <!-- 공통 CSS: contextPath 기준 -->
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-mypage.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/notification.css'/>">

    <%-- TODO: CSRF 토큰 (나중에 적용 시 주석 해제) --%>

    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>

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

            <!-- ▶ 알림 + 로그아웃 -->
            <div class="header-actions">
                <form action="${pageContext.request.contextPath}/auth/logout" method="post">
                    <%-- TODO: CSRF hidden input --%>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    <button type="submit" class="btn-logout">logout</button>
                </form>
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
        <div class="profile-img">
        <div class="avatar-lg">
                <img src="${user.profileImageUrl}" alt="${user.nickname}" class="avatar-lg"/>
        </div>
            <a class="upload text-center" href="<c:url value='/recipes/add'/>">Upload</a>
        </div>
        <div class="profile-info flex-box">
            <div class="profile-top">
                <div class="userInfo">
                <h2 class="profile-name">${user.userId}</h2>
                <a href="${pageContext.request.contextPath}/mypage/edit" class="edit-profile">✎ Edit</a>
                </div>
                <div class="profile-left">
                    <span>${user.nickname}</span>
                    <span class="location">${user.userLocation}</span>
                </div>
            </div>
            <div class="profile-right">
                <div class="follow-count">
                <a class="alink" href="<c:url value='/follow/network/${user.userId}'/>"><span class="f-text">Followers</span> <span class="fc-text">${followersCount} </span></a>
                    <a class="alink" href="<c:url value='/follow/network/${user.userId}'/>"><span class="f-text">Following</span> <span class="fc-text">${followingCount}</span></a>
                </div>
                <c:if test="${not empty user.interestTags}">
                    <div class="tags">
                        <c:forEach items="${user.interestTags}" var="tag">
                            <span class="chip">#${tag.tag}</span>
                        </c:forEach>
                    </div>
                </c:if>

            </div>
        </div>
    </section>

    <!-- 내 피드 -->
    <section class="container layout">
        <aside id="myposts" class="feed-list">
            <nav class="tabs">
                <button class="tab is-active" data-tab="myposts">My Feed</button>
                <button class="tab" data-tab="likes">Likes</button>
            </nav>

            <!-- 무한 스크롤 피드 / mypage-feed.js - innerHtml  -->
            <div id="feedContainer"></div>

        </aside>

        <!-- 좋아요 피드 -->
        <aside id="likes" class="feed-list hidden">
            <article class="card post">
                <div class="thumb"><img src="https://picsum.photos/seed/like1/800/500" alt="Liked Recipe"></div>
                <p class="muted">Liked: Chocolate Cake</p>
            </article>
        </aside>


        <!-- 팔로우/팔로잉 -->
        <aside id="myfollowing" class="feed-list sidebar">
            <div class="panel mb-8" id="airBox" data-ctx="${ctx}" data-user-sido="${user.userLocation}">
                <h3>🌫️오늘의 피크닉 지수</h3>
                <div class="airview card p-12">
                    <div class="air-row">
                        <label for="sido">지역</label>
                        <select id="sido" class="air-select">
                            <option>서울</option><option>부산</option><option>대구</option>
                            <option>인천</option><option>광주</option><option>대전</option>
                            <option>울산</option><option>세종</option><option>경기</option>
                            <option>강원</option><option>충북</option><option>충남</option>
                            <option>전북</option><option>전남</option><option>경북</option>
                            <option>경남</option><option>제주</option>
                        </select>
                    </div>
                    <div id="airText" class="air-text">불러오는 중…</div>
                </div>
            </div>

            <h2 class="section-title m-0">New</h2>
            <!-- 팔로우 탭 -->
            <nav class="tabs">
                <button class="tab is-active" data-tab="following">Following <%-- ${user.followingCount} --%></button>
                <button class="tab" data-tab="followers">Follower <%-- ${user.followerCount} --%></button>
            </nav>

            <!-- pc 버전에서만 보임 : 내 팔로잉/팔로우가 새로 올린 피드만 -->
            <div id="followContainer"></div>

        </aside>
    </section>
</main>

<!-- FAQ 플로팅 버튼 -->
<a id="faq-btn" class="faq-btn" href="<c:url value="faq" />">FAQ</a>
<div class="to-topbox">
    <button id="backToTop" class="to-top" aria-label="맨 위로">Top</button>
</div>

<%--<script src="${pageContext.request.contextPath}/js/notifs.js"></script>--%>
<script>
    // 전역은 여기 한 번만
    window.ctx = "${pageContext.request.contextPath}";
    window.currentUserEmail = "${currentUserEmail}";
</script>
<script src="${pageContext.request.contextPath}/js/mypage/airpanle.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/utils.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/mypage-feed.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/mypage-sidebar.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/position-fixed.js"></script>
<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<%--알림 js--%>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
</body>
</html>
