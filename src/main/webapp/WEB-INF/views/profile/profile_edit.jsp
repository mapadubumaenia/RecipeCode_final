<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 18.
  Time: 오후 2:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>프로필 편집</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-edit.css'/>">
</head>
<body>
<form action="<c:url value='/mypage/updateProfile'/>" method="post" enctype="multipart/form-data">
    <!-- CSRF 토큰 추가 -->
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<main class="container">
    <!-- 헤더: 기존 마이페이지/팔로우 리스트와 동일한 구조 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">Edit Profile</h1>
            <a class="float-text" href="<c:url value='/'/>">home</a>
        </div>
        <a class="btn-logout" href="<c:url value='/mypage'/>">Back</a>
    </header>

    <section class="form">
        <!-- 좌측: 폼 -->
        <main class="grid">
            <!-- 프로필 카드 -->
            <article class="card p-16">
                <h3 class="ttl">My profile</h3>

                <div class="avatar-row">
                    <label class="avatar-uploader">
                        <input id="avatar" type="file" name="profileImage" accept="image/*" />
                        <c:choose>
                            <c:when test="${not empty member.profileImageUrl}">
                                <img id="avatarPreview" src="${member.profileImageUrl}" class="avatar-lg"/>
                            </c:when>
                            <c:otherwise>
                                <span class="ph">프로필 이미지를 업로드</span>
                                <img id="avatarPreview" alt="" class="hidden"/>
                            </c:otherwise>
                        </c:choose>
                    </label>

                    <div class="id-block">
                        <div class="row">
                            <label class="label">아이디</label>
                            <input class="input" name="userId" value="<c:out value='${member.userId}' default=''/>" readonly />
                        </div>
                        <div class="row">
                            <label class="label">이메일</label>
                            <input class="input" name="userEmail" value="<c:out value='${member.userEmail}' default=''/>" readonly />
                        </div>
                    </div>
                </div>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">표시 이름</label>
                        <input class="input" name="nickname" value="<c:out value='${member.nickname}' default=''/>" placeholder="ex) 소금후추" />
                    </div>
                    <div class="row">
                        <label class="label">지역(옵션)</label>
                        <input class="input" name="userLocation" value="<c:out value='${member.userLocation}' default=''/>" placeholder="Seoul, KR" />
                    </div>
                </div>

                <div class="row">
                    <label class="label">한마디(소개)</label>
                    <textarea class="textarea" name="userIntroduce" placeholder="한 줄 소개를 적어주세요."><c:out value='${member.userIntroduce}' default=''/></textarea>
                </div>
            </article>

            <!-- 공개정보 & 링크 -->
            <article class="card p-16">
                <h3 class="ttl">공개 정보</h3>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">웹사이트</label>
                        <input class="input" name="userWebsite" value="<c:out value='${member.userWebsite}' default=''/>" placeholder="https://example.com" />
                    </div>
                    <div class="row">
                        <label class="label">인스타그램</label>
                        <input class="input" name="userInsta" value="<c:out value='${member.userInsta}' default=''/>" placeholder="@your_instagram" />
                    </div>
                </div>

                <div class="grid cols-2 mb-8">
                    <div class="row">
                        <label class="label">유튜브</label>
                        <input class="input" name="userYoutube" value="<c:out value='${member.userYoutube}' default=''/>" placeholder="채널/핸들" />
                    </div>
                    <div class="row">
                        <label class="label">블로그</label>
                        <input class="input" name="userBlog" value="<c:out value='${member.userBlog}' default=''/>" placeholder="티스토리/벨로그 등" />
                    </div>
                </div>

                <div class="row">
                    <label class="label mb-8">관심 태그</label>
                    <div class="tags mb-8" id="tagList"></div>
                    <input class="input mb-8"  id="interestTagInput" placeholder="관심 태그를 입력해주세요 — 예) 파스타" />
                    <button class="btn ghost" type="button" id="addTagBtn">+ 추가</button>
                </div>
                <!-- 보여줄 태그 리스트 -->
                <div class="tags" id="interestTagList">
                    <c:forEach var="t" items="${member.memberTags}" varStatus="ts">
                        <span class="tag" data-id="${t.tag.tagId}" data-tag="${t.tag.tag}">
                        <span>#${t.tag.tag}</span><span class="x" title="삭제">×</span>
                        </span>
                    </c:forEach>
                </div>

                <!-- 서버 전송용 hidden inputs -->
                <div id="interestTagHidden">
                    <c:forEach var="t" items="${member.memberTags}" varStatus="ts">
                        <c:if test="${!t.tag.deleted}">
                            <input type="hidden" name="interestTags[${ts.index}].tag" value="${t.tag.tag}"/>
                        </c:if>
                    </c:forEach>
                </div>
            </article>

            <!-- 환경설정 -->
            <article class="card p-16">
                <h3 class="ttl">환경설정</h3>
                <div class="grid cols-2 mb-8">
                    <div class="row">
                        <label for="pw" class="label">비밀번호(변경 시 입력)</label>
                        <div class="input-wrap mb-8">
                            <input class="input" type="password" id="pw" name="password" placeholder="8자 이상, 대/소문자+숫자 조합 권장" minlength="8" />
                            <button class="toggle" type="button" aria-label="비밀번호 표시" data-target="#pw">👁️</button>
                        </div>
                        <div id="pwMatchHint" class="hint muted"></div>
                    </div>
                    <div class="row">
                        <label class="label">비밀번호 확인</label>
                        <div class="input-wrap mb-8">
                        <input class="input" type="password" id="pw2" placeholder="다시 입력"  />
                        <button class="toggle" type="button" aria-label="비밀번호 표시" data-target="#pw2">👁️</button>
                        </div>
                    </div>
                </div>
                <div class="strength mb-8" aria-hidden="true"><span id="pwBar"></span></div>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">기본 공개 범위</label>
                        <select class="select" name="profileStatus">
                            <option value="PUBLIC"
                                    <c:if test="${member.profileStatus eq 'PUBLIC'}">selected</c:if>>Public</option>
                            <option value="FOLLOW"
                                    <c:if test="${member.profileStatus eq 'FOLLOW'}">selected</c:if>>Followers</option>
                        </select>
                    </div>
                </div>
                <div class="grid cols-2">
                    <c:forEach var="setting" items="${notiSettings}">
                        <label class="switch">
                            <input type="checkbox"
                                   name="noti_${setting.typeCode}"
                                   <c:if test="${setting.allow}">checked</c:if> />
                            <span>
                <c:choose>
                    <c:when test="${setting.typeCode eq 'FOLLOW'}">알림: 누군가 나를 팔로우</c:when>
                    <c:when test="${setting.typeCode eq 'COMMENT'}">알림: 내 레시피에 댓글</c:when>
                </c:choose>
            </span>
                        </label>
                    </c:forEach>
                </div>
            </article>

            <!-- 액션 -->
            <article class="card p-16">
                <div class="actions">
                    <button id="savebtn" type="submit" class="btn primary">저장</button>
                    <button id="cancelbtn" type="button" onclick="edit_cancel()" class="btn ghost">취소</button>
                    <span class="spacer"></span>
                    <button id="deletebtn" type="button" onclick="delete_account()" class="btn danger">회원탈퇴</button>
                </div>
                <div class="error" id="formError" aria-live="polite"></div>
            </article>

        </main>

        <!-- 우측: 가이드(데스크톱에서 고정) -->
        <aside class="help card p-16">
            <h3 class="ttl">작성 가이드</h3>
            <p class="tip"><strong>프로필 이미지</strong>는 정사각형 권장(최소 240×240).</p>
            <p class="tip"><strong>한마디</strong>는 50~120자 사이가 딱 보기 좋아요.</p>
            <p class="tip"><strong>링크</strong>는 실제 소셜/사이트만 넣어주세요.</p>
        </aside>
    </section>
</main>
</form>
<%--  JS  --%>
<script src="<c:url value='/js/mypage/mypage-edit.js'/>"></script>
<script src="<c:url value='/js/auth/password.js'/>"></script>
</body>
</html>
