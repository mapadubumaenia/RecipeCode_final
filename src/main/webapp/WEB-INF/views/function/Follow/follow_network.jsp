<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 23.
  Time: 오전 11:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
    <title>Follow Network</title>
  <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/follow-network.css'/>">

</head>
<body>
<main class="container profile-page">

  <!-- 헤더 -->
  <header class="container">
    <div class="flex-box">
      <div class="flex-row">
        <h1 class="page-title">Network</h1>
        <a href="/" class="float-text">home</a>
      </div>

      <!-- ▶ 알림 + 마이페이지 -->
      <div class="header-actions">
          <%-- TODO: CSRF hidden input (나중에 적용 시 주석 해제) --%>
          <%-- <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/> --%>
        <div class="notif-wrap">
          <!-- 알림 버튼 -->
          <sec:authorize access="isAuthenticated()">
            <sec:authentication property="principal" var="loginUser"/>
            <a class="alink" href="${pageContext.request.contextPath}/mypage">${loginUser.nickname}님</a>
          </sec:authorize>
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
  <section class="card profile-card flex-box">
    <div class="avatar-lg">
      <c:if test="${not empty profileOwner.profileImageUrl}">
        <img src="${profileOwner.profileImageUrl}" alt="${profileOwner.nickname}" class="avatar-lg"/>
      </c:if>
    </div>
    <div class="profile-info">
      <div class="profile-top">
        <h3 class="profile-name">${profileOwnerId} <span class="location">${profileOwner.userLocation}</span></h3>
      </div>
      <div class="profile-actions btn-none">
        <span class="f-text">Followers</span> <span class="fc-text"> ${followersCount} </span>
          <span class="f-text">Following</span> <span class="fc-text"> ${followingCount}</span>
      </div>
    </div>
    <div class="rightBox">
    <c:if test = "${!isSelf}">
      <button class="follow-btn ${isFollowingOwner ? 'is-following' : ''}"
              data-email="${profileOwnerEmail}"
              data-following="${isFollowingOwner}"
              data-uid="${profileOwnerId}"
      >${isFollowingOwner ? 'UnFollow' : 'Follow'}</button>
    </c:if>
    </div>
  </section>

  <!-- (옵션) 팔로잉 사용자 검색 -->
  <div class="search-bar search-following">
    <input type="text" id="searchInput" placeholder="유저 아이디 검색 (@username)" >
    <button id="searchBtn" class="btn">Search</button>
  </div>
  <div id="searchResult"></div>

  <!-- FOLLOW NETWORK LISTS -->
  <section class="network">
    <!-- 모바일에서만 보이는 탭 -->
    <nav class="tabs only-mobile">
      <button class="tab is-active" data-follow-tab="following">Following</button>
      <button class="tab" data-follow-tab="follower">Followers</button>
    </nav>

    <div class="lists">
      <section class="list-pane active" data-pane="following">
        <h3 class="pane-title">Following</h3>
        <div id="followingList"></div>
        <button id="btnMoreFollowing" class="btn more-btn">더보기</button>
      </section>

      <section class="list-pane" data-pane="follower">
        <h3 class="pane-title">Followers</h3>
        <div id="followerList"></div>
        <button id="btnMoreFollower" class="btn more-btn">더보기</button>
      </section>
    </div>
  </section>





</main>

<!-- FAQ 플로팅 버튼 -->
<a id="faq-btn" class="faq-btn" href="<c:url value="/faq" />">FAQ</a>

<script>
  const ctx = "${pageContext.request.contextPath}";
  const profileOwnerId = "${profileOwnerId}";
  const profileOwnerEmail = "${profileOwnerEmail}";
  const viewerEmail = "${currentUserEmail}";
  const isSelf = ${isSelf}; // true / false 그대로 출력
</script>
<script src="${pageContext.request.contextPath}/js/follow/network-searchResult.js"></script>
<script src="${pageContext.request.contextPath}/js/follow/network-view.js"></script>
<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
</body>
</html>
