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

            <!-- ▶ 알림 + 로그아웃 -->
            <div class="header-actions">
                <form action="${pageContext.request.contextPath}/logout" method="post">
                    <button type="submit" class="btn-logout">logout</button>
                </form>
                <div class="notif-wrap">
                    <button id="btnNotif" class="notif-btn" aria-haspopup="dialog"
                            aria-expanded="false" aria-controls="notifPanel" title="알림">
                        🔔
                        <span class="notif-dot" aria-hidden="true"></span>
                    </button>

                    <!-- 드롭다운 패널 -->
                    <div id="notifPanel" class="notif-panel" role="dialog" aria-label="알림 목록">
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
        <div class="avatar-lg">
            <c:if test="${not empty user.profileImageUrl}">
                <img src="${user.profileImageUrl}" alt="${user.nickname}" class="avatar-lg"/>
            </c:if>
        </div>
        <div class="profile-info">
            <div class="profile-top">
                <h2 class="profile-name">${user.nickname}</h2>
                <a href="${pageContext.request.contextPath}/mypage/edit" class="edit-profile">✎ Edit</a>
            </div>
            <div class="profile-actions btn-none">
                <a class="followbtn-md" href="${pageContext.request.contextPath}/mypage/followers">
                    Followers ${user.followerCount}
                </a>
                <a class="followbtn-md" href="${pageContext.request.contextPath}/mypage/following">
                    Following ${user.followingCount}
                </a>
            </div>
        </div>
    </section>

    <!-- (옵션) 팔로잉 사용자 검색 -->
        <div class="search-bar search-following">
            <input type="text" id="searchInput" placeholder="팔로잉한 사용자 아이디 검색 (@username)" >
            <button id="searchBtn" class="btn">Search</button>
        </div>
        <div id="searchResult"></div>

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
            <h2 class="section-title m-0">New</h2>
            <!-- 팔로우 탭 -->
            <nav class="tabs">
                <button class="tab is-active" data-tab="following">Following ${user.followingCount}</button>
                <button class="tab" data-tab="followers">Follower ${user.followerCount}</button>
            </nav>

            <!-- pc 버전에서만 보임 : 내 팔로잉/팔로우가 새로 올린 피드만 -->
            <div id="followContainer"></div>

        </aside>
    </section>
</main>

<!-- FAQ 플로팅 버튼 -->
<button id="faq-btn" class="faq-btn">FAQ</button>

<%--<script src="${pageContext.request.contextPath}/js/notifs.js"></script>--%>
<%--<script src="${pageContext.request.contextPath}/js/position-fixed.js"></script>--%>
<%--<script>const currentUserEmail = "${user.userEmail}";</script>--%>
<script>const currentUserEmail = "asdf1234@naver.com";</script>
<script src="${pageContext.request.contextPath}/js/mypage/utils.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/mypage-searchResult.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/mypage-feed.js"></script>
<script src="${pageContext.request.contextPath}/js/mypage/mypage-sidebar.js"></script>
</body>
</html>
