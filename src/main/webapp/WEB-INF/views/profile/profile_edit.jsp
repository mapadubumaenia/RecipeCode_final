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
    <title>프로필 편집</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-edit.css'/>">
</head>
<body>
<main class="container">

    <!-- 헤더: 기존 마이페이지/팔로우 리스트와 동일한 구조 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">Edit Profile</h1>
            <a class="float-text" href="newfeed-ver-main-wireframe.html">home</a>
        </div>
        <a class="btn-logout" href="newfeed-ver-mypage-wireframe.html">Back</a>
    </header>

    <section class="form">
        <!-- 좌측: 폼 -->
        <main class="grid">
            <!-- 프로필 카드 -->
            <article class="card p-16">
                <h3 class="ttl">My profile</h3>

                <div class="avatar-row">
                    <label class="avatar-uploader">
                        <input id="avatar" type="file" accept="image/*" />
                        <span class="ph">프로필 이미지를 업로드</span>
                        <!-- 미리보기 이미지는 JS 붙이면 보여짐 -->
                        <img id="avatarPreview" alt="" class="hidden" />
                    </label>

                    <div class="id-block">
                        <div class="row">
                            <label class="label">아이디</label>
                            <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userId}' default=''/>" readonly />
                        </div>
                        <div class="row">
                            <label class="label">이메일</label>
                            <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userEmail}' default=''/>" readonly />
                        </div>
                    </div>
                </div>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">표시 이름</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.nickname}' default=''/>" placeholder="ex) 소금후추" />
                    </div>
                    <div class="row">
                        <label class="label">지역(옵션)</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userLocation}' default=''/>" placeholder="Seoul, KR" />
                    </div>
                </div>

                <div class="row">
                    <label class="label">한마디(소개)</label>
                    <textarea class="textarea" placeholder="한 줄 소개를 적어주세요."><c:out value='${pageContext.request.userPrincipal.principal.member.userIntroduce}' default=''/></textarea>
                </div>
            </article>

            <!-- 공개정보 & 링크 -->
            <article class="card p-16">
                <h3 class="ttl">공개 정보</h3>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">웹사이트</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userWebsite}' default=''/>" placeholder="https://example.com" />
                    </div>
                    <div class="row">
                        <label class="label">인스타그램</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userInsta}' default=''/>" placeholder="@your_instagram" />
                    </div>
                </div>

                <div class="grid cols-2 mb-8">
                    <div class="row">
                        <label class="label">유튜브</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userYoutube}' default=''/>" placeholder="채널/핸들" />
                    </div>
                    <div class="row">
                        <label class="label">블로그</label>
                        <input class="input" value="<c:out value='${pageContext.request.userPrincipal.principal.member.userBlog}' default=''/>" placeholder="티스토리/벨로그 등" />
                    </div>
                </div>

                <div class="row">
                    <label class="label mb-8">관심 태그</label>
                    <div class="tags mb-8" id="tagList"></div>
                    <input class="input mb-8" id="tagInput" placeholder="레시피 태그를 입력해주세요 — 예) 파스타" />
                    <button class="btn ghost" type="button" id="addTagBtn">+ 추가</button>
                </div>

            </article>

            <!-- 환경설정 -->
            <article class="card p-16">
                <h3 class="ttl">환경설정</h3>

                <div class="grid cols-2">
                    <div class="row">
                        <label class="label">기본 공개 범위</label>
                        <select class="select" name="profileStatus">
                            <option value="PUBLIC"
                                    <c:if test="${pageContext.request.userPrincipal.principal.member.profileStatus eq 'PUBLIC'}">selected</c:if>>Public</option>
                            <option value="FOLLOW"
                                    <c:if test="${pageContext.request.userPrincipal.principal.member.profileStatus eq 'FOLLOW'}">selected</c:if>>Followers</option>
                            <option value="PRIVATE"
                                    <c:if test="${pageContext.request.userPrincipal.principal.member.profileStatus eq 'PRIVATE'}">selected</c:if>>Private</option>
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
                    <button class="btn primary">저장</button>
                    <button class="btn ghost">취소</button>
                    <span class="spacer"></span>
                    <button class="btn danger">회원탈퇴</button>
                </div>
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

<script>
    /* ===== 공통 유틸 ===== */
    // $() -> document.querySelector(s) 로 정의 해놓은 것
    const $ = (s, el= document) => el.querySelector(s);
    const $$ = (s, el = document) => [...el.querySelectorAll(s)];

    /* ===== 프로필 이미지 미리보기 ===== */
    $("#avatar")?.addEventListener("change", (e) =>{
        const file = e.target.files?.[0];
        const img = $("#avatarPreview");
        if(!file) {
            img?.classList.add("hidden");
            return;
        }
        const url = URL.createObjectURL(file);
        img.src = url;
        img.classList.remove("hidden");
        $(".avatar-uploader .ph").classList.add("hidden");
    });

    /* ===== 태그 ===== */
    const tagInput = $("#tagInput");
    const tagList = $("#tagList");

    function addTag(text) {
        const label = text.trim().replace(/^#+/, "");
        if (!label) return;
        // 중복 방지
        const norm = label.toLowerCase(); // 소문자 기준
        if ([...tagList.children].some((el) => el.dataset.tag === label))
            return;

        const el = document.createElement("span");
        el.className = "tag";
        el.dataset.tag = norm;
        el.innerHTML = `<span>#${label}</span><span class="x" title="삭제">×</span>`;
        tagList.appendChild(el);
        tagInput.value = "";
    }
    tagInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            addTag(tagInput.value);
        }
    });
    $("#addTagBtn")?.addEventListener("click", () => addTag(tagInput.value));
    tagList?.addEventListener("click", (e) => {
        e.preventDefault();
        addTag(tagInput.value);
        const x = e.target.closest(".x");
        if (!x) return;
        x.parentElement.remove();
    });

</script>
</body>
</html>
