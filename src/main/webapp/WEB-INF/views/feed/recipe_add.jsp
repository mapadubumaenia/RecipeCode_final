<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 12.
  Time: 오전 10:25
  To change this template use File | Settings | File Templates.
--%>
<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 12.
  Time: 오전 10:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>레시피 등록/수정</title>
    <!-- 공통 리소스 contextPath 기준 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/create-update.css" />
</head>
<body>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="recipe" value="${recipe}"/>
<c:set var="isEdit" value="${not empty recipe && not empty recipe.uuid}"/>

<div class="container">
    <!-- 헤더 (통일감 유지) -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title"><c:choose><c:when test="${isEdit}">Update</c:when><c:otherwise>Create</c:otherwise></c:choose></h1>
            <a class="float-text" href="${contextPath}/">home</a>
        </div>
        <button type="button" class="btn-logout" onclick="history.back()">Back</button>
    </header>


    <!-- form 시작: 서버 렌더링/전송 -->
    <form id="recipeForm" enctype="multipart/form-data" method="post"
          action="<c:choose>
            <c:when test='${isEdit}'>
                ${contextPath}/recipes/${recipe.uuid}</c:when>
                <c:otherwise>${contextPath}/recipes</c:otherwise>
            </c:choose>">
        <c:if test="${isEdit}">
            <!-- PUT 메서드 흉내 (Spring MVC @PutMapping 과 호환 시) -->
            <input type="hidden" name="_method" value="PUT"/>
            <input type="hidden" name="uuid" value="${recipe.uuid}"/>
        </c:if>


        <!-- Spring Security CSRF(있다면) -->
<%--        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>--%>


        <!-- 폼 상태 관리용 hidden -->
        <input type="hidden" id="postStatus" name="postStatus"
               value="<c:out value='${empty recipe.postStatus ? "PUBLIC" : recipe.postStatus}'/>"/>
        <input type="hidden" id="recipeType" name="recipeType"
               value="<c:out value='${empty recipe.recipeType ? "IMAGE" : recipe.recipeType}'/>"/>


        <section class="form">
            <!-- 메인 폼 -->
            <main class="grid">
                <!-- 작성 유형 탭 -->
                <article class="card p-16">
                    <div class="tabs" role="tablist" aria-label="레시피 유형 선택">
                        <button class="tab" id="tabImage" role="tab" aria-selected="true">이미지 레시피</button>
                        <button class="tab" id="tabVideo" role="tab" aria-selected="false">동영상 레시피</button>
                    </div>
                    <p class="tab-help"> 탭 전환 시 내용이 사라질 수 있으니 주의하세요! 🙌</p>
                </article>

                <!-- 썸네일 -->
                <article class="card p-16">
                    <h3 style="margin:0 0 8px; font-weight:800">대표 이미지</h3>
                    <div class="thumb-uploader">
                        <%-- 이미지모드 섬네일 --%>
                        <label class="thumb" id="thumbBox">
                            <input id="thumb" name="thumbnail" type="file" accept="image/*" />
                            <span class="ph <c:if test='${not empty recipe.thumbnailUrl}'>hidden</c:if>'">썸네일을 업로드하세요</span>
                            <img id="thumbPreview" alt=""
                                 class="<c:if test='${empty recipe.thumbnailUrl}'>hidden</c:if>"
                                 <c:if test='${not empty recipe.thumbnailUrl}'>src='${recipe.thumbnailUrl}'</c:if> />
                        </label>

                            <!-- VIDEO 모드일 때 썸네일 자리에 뜨는 프리뷰 -->
                            <div id="videoThumbBox" class="ratio-16x9 hidden" aria-hidden="true">
                                <iframe id="videoThumbFrame" title="동영상 미리보기" allowfullscreen
                                        referrerpolicy="strict-origin-when-cross-origin"></iframe>
                            </div>


                        <div class="meta-row">
                            <input class="input" id="title" name="recipeTitle" maxlength="80"
                                   value="<c:out value='${recipe.recipeTitle}'/>" placeholder="레시피 제목"/>
                            <input class="input" id="subtitle" name="recipeIntro"
                                   value="<c:out value='${recipe.recipeIntro}'/>" placeholder="간단 설명 (선택)"/>
                        </div>
                    </div>
                </article>

                <!-- 동영상 레시피 Pane -->
                <article class="card p-16 hidden" id="videoPane" aria-hidden="true">
                    <h3 style="margin:0 0 12px; font-weight:800">동영상 레시피</h3>
                    <div class="video-form">
                        <input class="input" id="videoUrl" name="videoUrl"
                               value="<c:out value='${recipe.videoUrl}'/>"
                               placeholder="동영상 URL을 붙여넣기 (예: https://youtu.be/VIDEO_ID)" />
                        <div class="video-preview" id="videoPreviewWrap">
                            <div class="ratio-16x9">
                                <iframe id="videoPreview" title="동영상 미리보기" allowfullscreen referrerpolicy="strict-origin-when-cross-origin"></iframe>
                            </div>
                            <p class="muted small" id="videoHint">YouTube 링크는 자동으로 미리보기가 보여요. 기타 플랫폼은 임베드 허용 여부에 따라 미리보기가 제한될 수 있어요.</p>
                        </div>
                        <textarea class="input" id="videoText" name="videoText" placeholder="설명(예: 조리 포인트, 대체 재료, 주의사항 등)"><c:out value='${recipe.videoText}'/></textarea>
                    </div>
                </article>

                <!-- 메타 정보 -->
                <article class="card p-16 meta">
                    <h3 style="margin:0 0 8px; font-weight:800">메타 정보</h3>
                    <div class="row">
                        <select class="select" id="category" name="recipeCategory">
                            <option value="">카테고리 선택</option>
                            <c:set var="cat" value="${recipe.recipeCategory}"/>
                            <option value="한식" <c:if test='${cat=="한식"}'>selected</c:if>>한식</option>
                            <option value="양식" <c:if test='${cat=="양식"}'>selected</c:if>>양식</option>
                            <option value="중식" <c:if test='${cat=="중식"}'>selected</c:if>>중식</option>
                            <option value="디저트" <c:if test='${cat=="디저트"}'>selected</c:if>>디저트</option>
                            <option value="비건" <c:if test='${cat=="비건"}'>selected</c:if>>비건</option>
                        </select>
                        <input class="input" id="time" name="cookingTime" type="number" min="0"
                               value="<c:out value='${recipe.cookingTime}'/>" placeholder="조리시간(분)" />
                    </div>
                    <div class="row">
                        <c:set var="diff" value="${recipe.difficulty}"/>
                        <select class="select" id="difficulty" name="difficulty">
                            <option value="">난이도</option>
                            <option value="쉬움" <c:if test='${diff=="쉬움"}'>selected</c:if>>쉬움</option>
                            <option value="보통" <c:if test='${diff=="보통"}'>selected</c:if>>보통</option>
                            <option value="어려움" <c:if test='${diff=="어려움"}'>selected</c:if>>어려움</option>
                        </select>
                        <!-- 공개 스위치 -->
                        <label class="switch">
                            <input id="isPublic" type="checkbox"
                                   <c:if test='${empty recipe.postStatus || recipe.postStatus=="PUBLIC"}'>checked</c:if> />
                            <span>공개 (끄면 비공개/임시)</span>
                        </label>
                    </div>
                </article>

                <!-- 재료 -->
                <article class="card p-16">
                    <h3 style="margin:0 0 8px; font-weight:800">재료</h3>
                    <div class="items" id="ingredients">
                        <c:choose>
                            <c:when test="${not empty recipe.ingredients}">
                                <c:forEach var="ing" items="${recipe.ingredients}" varStatus="s">
                                    <div class="item-row" data-index="${s.index}">
                                        <input class="input" name="ingredients[${s.index}].ingredientName" value="<c:out value='${ing.ingredientName}'/>" placeholder="예) 스파게티 면" />
                                        <input class="input" name="ingredients[${s.index}].ingredientAmount" value="<c:out value='${ing.ingredientAmount}'/>" placeholder="예) 200g" />
                                        <input type="hidden" name="ingredients[${s.index}].sortOrder" value="${s.index + 1}" />
                                        <button type="button" class="btn icon del">🗑</button>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <!-- 기본 1행 -->
                                <div class="item-row" data-index="0">
                                    <input class="input" name="ingredients[0].ingredientName" placeholder="예) 스파게티 면" />
                                    <input class="input" name="ingredients[0].ingredientAmount" placeholder="예) 200g" />
                                    <input type="hidden" name="ingredients[0].sortOrder" value="1" />
                                    <button type="button" class="btn icon del">🗑</button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div style="margin-top:8px">
                        <button type="button" class="btn ghost" id="addIng">+ 재료 추가</button>
                    </div>
                </article>

                <!-- 태그 -->
                <article class="card p-16">
                    <h3 style="margin:0 0 8px; font-weight:800">태그</h3>
                    <div class="tag-input">
                        <input class="input mb-12" id="tagInput" placeholder="태그 입력 후 Enter" />
                        <button type="button" class="btn ghost" id="addTagBtn">+ 추가</button>
                    </div>
                    <div class="tags" id="tagList">
                        <c:forEach var="t" items="${recipe.tags}" varStatus="ts">
                            <span class="tag" data-tag="${t.tag}">
                                <span>#${t.tag}</span><span class="x" title="삭제">×</span></span>
                        </c:forEach>
                    </div>
                    <!-- 서버 전송용 hidden inputs -->
                    <div id="tagHidden">
                        <c:forEach var="t" items="${recipe.tags}" varStatus="ts">
                            <input type="hidden" name="tags[${ts.index}].tag" value="${t.tag}"/>
                        </c:forEach>
                    </div>
                </article>


                <!-- 하단 액션 -->
                <article class="card p-16">
                    <div class="actions">
                        <button class="btn" type="button" id="btnCancel">취소</button>
                        <button class="btn" id="saveDraft" type="button">임시 저장</button>
                        <button class="btn primary" id="publish" type="button"><c:choose><c:when test='${isEdit}'>수정 완료</c:when><c:otherwise>발행</c:otherwise></c:choose></button>
                    </div>
                </article>

                <!-- 조리 단계 (드래그 정렬) — 이미지 레시피 Pane -->
                <article class="card p-16" id="imagePane">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
                        <h3 style="margin:0;font-weight:800">조리 단계</h3>
                        <button type="button" class="btn ghost" id="addStep">+ 추가</button>
                    </div>
                    <div class="steps" id="steps">
                        <c:choose>
                        <c:when test="${not empty recipe.contents}">
                            <c:forEach var="c" items="${recipe.contents}" varStatus="cs">
                                <article class="step" data-index="${cs.index}">
                                    <div class="step-head">
                                        <div class="step-title">Step <span class="no">${cs.index + 1}</span></div>
                                        <div style="display:flex;gap:8px">
                                            <button type="button" class="btn icon move-top" title="맨 위로">⤒</button>
                                            <button type="button" class="btn icon move-up" title="위로">↑</button>
                                            <button type="button" class="btn icon move-down" title="아래로">↓</button>
                                            <button type="button" class="btn icon move-bottom" title="맨 아래로">⤓</button>
                                            <button type="button" class="btn icon add-below" title="아래에 단계 추가">＋</button>
                                            <button type="button" class="btn icon danger del" title="삭제">🗑</button>
                                        </div>
                                    </div>
                                    <div class="step-body">
                                        <label class="upload">
                                            <input type="file" name="stepImages" accept="image/*" />
                                            <span class="ph <c:if test='${not empty c.recipeImageUrl}'>hidden</c:if>'">이미지 업로드</span>
                                            <img class="<c:if test='${empty c.recipeImageUrl}'>hidden</c:if>" alt=""
                                                 <c:if test='${not empty c.recipeImageUrl}'>src='${c.recipeImageUrl}'</c:if> />
                                        </label>
                                        <textarea name="contents[${cs.index}].stepExplain" placeholder="설명(예: 팬에 올리브오일을 두르고 마늘을 볶습니다.)"><c:out value='${c.stepExplain}'/></textarea>
                                        <input type="hidden" name="contents[${cs.index}].stepOrder" value="${cs.index + 1}"/>
                                    </div>
                                </article>
                            </c:forEach>
                        </c:when>
                            <c:otherwise>
                            <!-- 기본 2개 샘플 -->
                            <article class="step" data-index="0">
                                <div class="step-head">
                                    <div class="step-title">Step <span class="no">1</span></div>
                                    <div style="display:flex;gap:8px">
                                        <button type="button" class="btn icon move-top" title="맨 위로">⤒</button>
                                        <button type="button" class="btn icon move-up" title="위로">↑</button>
                                        <button type="button" class="btn icon move-down" title="아래로">↓</button>
                                        <button type="button" class="btn icon move-bottom" title="맨 아래로">⤓</button>
                                        <button type="button" class="btn icon add-below" title="아래에 단계 추가">＋</button>
                                        <button type="button" class="btn icon danger del" title="삭제">🗑</button>
                                    </div>
                                </div>
                                <div class="step-body">
                                    <label class="upload">
                                        <input type="file" name="stepImages" accept="image/*" />
                                        <span class="ph">이미지 업로드</span>
                                        <img class="hidden" alt="">
                                    </label>
                                    <textarea name="contents[0].stepExplain" placeholder="설명(예: 팬에 올리브오일을 두르고 마늘을 볶습니다.)"></textarea>
                                    <input type="hidden" name="contents[0].stepOrder" value="1"/>
                                </div>
                            </article>
                            <article class="step" data-index="1">
                                <div class="step-head">
                                    <div class="step-title">Step <span class="no">2</span></div>
                                    <div style="display:flex;gap:8px">
                                        <button type="button" class="btn icon move-top" title="맨 위로">⤒</button>
                                        <button type="button" class="btn icon move-up" title="위로">↑</button>
                                        <button type="button" class="btn icon move-down" title="아래로">↓</button>
                                        <button type="button" class="btn icon move-bottom" title="맨 아래로">⤓</button>
                                        <button type="button" class="btn icon add-below" title="아래에 단계 추가">＋</button>
                                        <button type="button" class="btn icon danger del" title="삭제">🗑</button>
                                    </div>
                                </div>
                                <div class="step-body">
                                    <label class="upload">
                                        <input type="file" name="stepImages" accept="image/*" />
                                        <span class="ph">이미지 업로드</span>
                                        <img class="hidden" alt="">
                                    </label>
                                    <textarea name="contents[1].stepExplain" placeholder="설명(예: 물 1L를 끓입니다.)"></textarea>
                                    <input type="hidden" name="contents[1].stepOrder" value="2"/>
                                </div>
                            </article>
                        </c:otherwise>
                        </c:choose>
                    </div>
                </article>

</main>


<!-- 작성 가이드 / 사이드 -->
<aside class="card p-16 help">
    <h3 style="margin:0 0 8px; font-weight:800">작성 가이드</h3>
    <p class="tip"><strong>제목은 핵심만</strong> (검색에 잘 걸리게)</p>
    <p class="tip">대표 이미지는 가로 비율(16:9)을 추천해요.</p>
    <p class="tip">재료와 단계는 <strong>+</strong> 버튼으로 자유롭게 추가/삭제!</p>
    <p class="tip">발행 전 <strong>임시저장</strong>으로 초안을 안전하게 보관.</p>
</aside>
</section>
</form>
</div>


<!-- JSP에서 contextPath 전역 변수 내려주기 (필요 시) -->
<script>window.contextPath = '${contextPath}';</script>
<!-- 기능 스크립트: 뷰 렌더링(폼 전송) 방식 -->
<script src="${contextPath}/js/recipe-create-update.js"></script>

</body>
</html>
