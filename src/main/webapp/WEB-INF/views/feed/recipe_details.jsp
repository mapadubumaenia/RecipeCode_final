<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 12.
  Time: 오전 11:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- 날짜 포맷을 쓰고 싶으면 fmt를 추가하고, LocalDateTime -> String 변환은 컨버터/DTO에서 처리 권장
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
--%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <!-- JSP <head> 안에 추가 (스프링 시큐리티 쓰면 제공됨) -->
<%--    <meta name="_csrf" content="${_csrf.token}"/>--%>
<%--    <meta name="_csrf_header" content="${_csrf.headerName}"/>--%>
    <title><c:out value="${recipe.recipeTitle}"/> - Details</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${ctx}/css/common.css" />
    <link rel="stylesheet" href="${ctx}/css/recipe-details.css" />
</head>
<body>
<div class="container" data-recipe-uuid="<c:out value='${recipe.uuid}'/>">
    <!-- 헤더 -->
    <header class="container">
        <div class="flex-box">
            <div class="flex-row">
                <h1 class="page-title">Details</h1>
                <a href="${ctx}/" class="float-text">home</a>
            </div>

            <div class="header-actions">
                <a class="register">👤</a>
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
                                <button class="btn small" id="markAll">모두 읽음</button>
                            </div>
                        </div>

                        <div class="notif-list" id="notifList"><!-- JS 렌더 --></div>

                        <div class="notif-foot">
                            <button class="btn small" id="closeNotif">닫기</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </header>

    <!-- MAIN -->
    <div class="layout">
        <main>
            <!-- 메인 콘텐츠 -->
            <section class="contentBar content mb-12">
                <div class="mb-12">
                    <h1 class="title">
                        <c:out value="${recipe.recipeTitle}" />
                    </h1>
                    <div class="meta">
                        조회수 <c:out value="${recipe.viewCount}" />회 ·
                        <c:out value="${insertTime}" /> 업로드 ·
                        조리시간 <c:out value="${recipe.cookingTime}" />분
                    </div>
                </div>

                    <!-- 메인 비주얼 영역 -->
        <c:choose>
            <c:when test="${isVideo}">
                 <div class="ratio-16x9 mb-12">
                     <iframe src="${embedUrl}" allowfullscreen
                             referrerpolicy="strict-origin-when-cross-origin"
                             style="width:100%;height:100%;border:0"></iframe>
                 </div>
            </c:when>
            <c:otherwise>
                    <!-- 이미지/텍스트 슬라이드 -->
                <div class="step-slider mb-12">
                    <div class="slides" id="imgSlides">
                        <c:forEach var="c" items="${recipe.contents}">
                               <c:set var="imgSrc" value="${c.recipeImageUrl}"/>
                                     <c:if test="${fn:startsWith(imgSrc, '/')}">
                                         <c:set var="imgSrc" value="${ctx}${imgSrc}"/>
                                    </c:if>
                               <div class="slide"><img src="${imgSrc}" alt="" /></div>
                        </c:forEach>
                        <!-- 컨텐츠 이미지 없으면 썸네일/플레이스홀더 -->
                        <div class="slide">
                            <c:set var="thumbSrc" value="${recipe.thumbnailUrl}"/>
                            <c:if test="${fn:startsWith(thumbSrc, '/')}">
                                <c:set var="thumbSrc" value="${ctx}${thumbSrc}"/>
                            </c:if>
                            <c:if test="${empty thumbSrc}">
                                <c:set var="thumbSrc" value="https://placehold.co/600x400"/>
                            </c:if>
                            <img src="${thumbSrc}" alt="대표 이미지"/>
                        </div>
                    </div>
                    <button class="prev" type="button" aria-label="이전">◀</button>
                    <button class="next" type="button" aria-label="다음">▶</button>
                </div>
            </c:otherwise>
        </c:choose>
            <!-- 조리 순서: VIDEO면 숨김 -->
            <c:if test="${not isVideo}">
                <aside class="panel mb-12">
                    <h3>👣 조리 순서</h3>
                    <div id="textPanel">
                        <div class="text-viewport">
                            <div class="slides" id="textSlides">
                                <c:choose>
                                    <c:when test="${not empty recipe.contents}">
                                        <c:forEach var="c" items="${recipe.contents}">
                                            <div class="slide">
                                                <p><c:out value="${c.stepExplain}" /></p>
                                            </div>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="slide"><p>등록된 조리 단계가 없습니다.</p></div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </aside>
            </c:if>
                <aside class="panel">
                    <h3>🧾 재료</h3>
                    <ul class="grid">
                        <c:choose>
                            <c:when test="${not empty recipe.ingredients}">
                                <c:forEach var="ing" items="${recipe.ingredients}">
                                    <li>
                                        <c:out value="${ing.ingredientName}" />
                                        <c:if test="${not empty ing.ingredientAmount}"> <c:out value="${ing.ingredientAmount}" /></c:if>
                                    </li>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <li>등록된 재료가 없습니다.</li>
                            </c:otherwise>
                        </c:choose>
                    </ul>
                </aside>
            </section>

            <!-- 채널바 / 액션바 -->
            <section class="channelBar">
                <div class="channel mb-8">
                    <div class="channelInfo">
                        <img class="avatar" src="<c:out value="${recipe.profileImageUrl}" />" alt="" />
                        <div>
                            <strong><c:out value="${recipe.userId}" /></strong>
                            <div class="meta">
                                <c:out value="${recipe.recipeCategory}" /> ·
                                <c:out value="${recipe.difficulty}" />
                            </div>
                        </div>
                    </div>
                    <c:set var="isOwner" value="${viewerEmail != null && viewerEmail == recipe.userEmail}" />
                    <c:choose>
                        <c:when test="${isOwner}"><%-- 본인글버튼없음 --%></c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${empty viewerEmail}">
                                <%-- 게스트 --%>
                                <a class="followbtn-sm"
                                   id="btnFollow"
                                   data-owner="${recipe.userEmail}"
                                   data-following="false"
                                   aria-disabled="true"
                                   title="로그인이 필요합니다">Follow</a>
                            </c:when>
                            <c:otherwise>
                                <%-- 로그인 사용자 --%>
                                <a class="followbtn-sm ${recipe.followingOwner ? 'is-following' : ''}"
                                   id="btnFollow"
                                   data-owner="${recipe.userEmail}"
                                   data-following="${recipe.followingOwner}"
                                   aria-pressed="${recipe.followingOwner}">
                                    ${recipe.followingOwner ? 'Unfollow' : 'Follow'}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:otherwise>
                    </c:choose>

                </div>

                <div class="actions">

                    <button id="btnLike"
                            class="like like-toggle btn-none ${recipe.liked ? 'active' : ''}"
                            data-uuid="${recipe.uuid}"
                            data-like="${recipe.liked ? 'true' : 'false'}"
                            aria-pressed="${recipe.liked}"
                    ${isOwner ? 'aria-disabled="true" title="본인 레시피에는 좋아요를 누를 수 없습니다."' : ''}>
                        <span class="icon" aria-hidden="true"></span>
                        <span class="cnt">${recipe.likeCount}</span>
                    </button>

                    <button class="btn-none share-btn float-text" data-uuid="${recipe.uuid}">🔗공유</button>
                    <button class="btn-none" id="btnReport">🚩 신고</button>
                </div>
            </section>

            <!-- 본문/태그 -->
            <section class="desc" id="postDesc">
                <div class="tags">
                    <c:forEach var="t" items="${recipe.tags}">
                        <span class="tag">#<c:out value="${t.tag}" /></span>
                    </c:forEach>
                </div>
                <div class="contentText"><c:out value="${recipe.recipeIntro}" /></div>
                <button type="button" class="btn-none toggle" id="btnToggleDesc">더보기</button>
            </section>

            <!-- 댓글 (AJAX 예정) -->
            <section class="comments" id="comments">
                <h3 class="comments-title">💬 댓글 <span class="count"><c:out value="${recipe.commentCount}" /></span></h3>
                <div class="comment-input">
                    <img class="avatar-sm" src="${ctx}/images/avatar-placeholder.png" alt="" />
                    <label class="sr-only" for="cmt"></label>
                    <textarea id="cmt" placeholder="따끈한 피드백 남기기..."></textarea>
                    <button type="button" class="btn" id="btnCmtSubmit">등록</button>
                </div>

                <!-- 목록은 추후 AJAX로 -->
                <div class="comments-box" data-show-count="3">
                    <div class="list" id="cmtList"></div>
                    <button type="button" class="toggle toggleBox" id="btnCmtMore">더보기</button>
                </div>
            </section>
        </main>

        <!-- 사이드 추천 (옵션) -->
        <aside class="side" id="sideList" aria-live="polite">
            <div class="loader" id="loader" hidden>
                <div class="spinner"></div>
                더 불러오는 중…
            </div>
            <div id="sentinel" style="height: 1px"></div>
        </aside>
    </div>
</div>

<script>
    const ctx = "${pageContext.request.contextPath}";
</script>
<script src="${ctx}/js/recipes/recipe-details.js"></script>
<script src="${ctx}/js/recipes/recipe-detailside.js"></script>

<div id="myReportModal" class="modal" hidden>
    <div class="modal-content">
        <h3> 댓글 신고</h3>
        <form id="myReportForm">
            <input type="hidden" name="commentsId" id="commentsId" value="">
            <div>
                <label>신고 유형</label>
                <label>
                    <select name="reportType" required>
                        <option value="0">욕설</option>
                        <option value="1">스팸</option>
                        <option value="2">저작권</option>
                    </select>
                </label>
            </div>
            <div>
                <label>신고 사유</label>
                <label>
                    <textarea name="reason" rows="4" maxlength="500" placeholder="신고 사유를 입력해주세요." required></textarea>
                </label>
            </div>
            <button type="submit">제출</button>
            <button type="button" id="myReportClose">취소</button>
        </form>
    </div>
</div>


<%-- TODO: 신고 모달 --%>
<div id="reportModal" class="modal" hidden>
    <div class="modal-content report-modal">
        <h3 class="modal-title">🚩 레시피 신고</h3>
        <form id="reportForm">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="uuid" value="${recipe.uuid}"/>

            <div class="form-group">
                <label class="form-label">신고 유형</label>
                <div class="radio-group">
                    <label><input type="radio" name="reportType" value="0" required> 욕설</label>
                    <label><input type="radio" name="reportType" value="1"> 스팸</label>
                    <label><input type="radio" name="reportType" value="2"> 저작권</label>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="reason">사유</label>
                <textarea name="reason" id="reason" rows="4" maxlength="500" placeholder="신고 사유를 입력해주세요."
                          required></textarea>
            </div>

            <div class="modal-actions">
                <button type="submit" class="btn-submit">제출</button>
                <button type="button" id="btnClose" class="btn-cancel">취소</button>
            </div>
        </form>
    </div>
</div>

<%--<script>--%>
<%--    const ctx = "${pageContext.request.contextPath}";--%>
<%--</script>--%>
<!-- jQuery CDN -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<%--알림 js--%>
<script src="<c:url value='/js/mypage/utils.js'/>"></script>
<script src="${pageContext.request.contextPath}/js/notification.js"></script>
</body>
</html>
