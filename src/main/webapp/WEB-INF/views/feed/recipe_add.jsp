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
<div class="container">
    <!-- 헤더 (통일감 유지) -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">Create</h1>
            <a class="float-text" href="newfeed-ver-main-wireframe.html">home</a>
        </div>
        <a class="btn-logout" href="newfeed-ver-main-wireframe.html">Back</a>
    </header>
    <!-- form 시작 -->
    <form id="recipeForm" enctype="multipart/form-data" novalidate>

    <section class="form">
        <!-- 메인 폼 -->
        <main class="grid">
            <!-- 작성 유형 탭 -->
            <article class="card p-16">
                <div class="tabs" role="tablist" aria-label="레시피 유형 선택">
                    <button class="tab is-active" id="tabImage" role="tab" aria-selected="true">이미지 레시피</button>
                    <button class="tab" id="tabVideo" role="tab" aria-selected="false">동영상 레시피</button>
                </div>
                <p class="tab-help"> 탭 전환 시 내용이 사라질 수 있으니 주의하세요! 🙌</p>
            </article>
            <!-- 썸네일 -->
            <article class="card p-16">
                <h3 style="margin: 0 0 8px; font-weight: 800">대표 이미지</h3>
                <div class="thumb-uploader">
                    <label class="thumb">
                        <input id="thumb" type="file" accept="image/*" />
                        <span class="ph">썸네일을 업로드하세요</span>
                        <img id="thumbPreview" alt="" class="hidden" />
                    </label>
                    <div class="meta-row">
                        <input
                                class="input"
                                id="title"
                                placeholder="레시피 제목"
                                maxlength="80"
                        />
                        <input
                                class="input"
                                id="subtitle"
                                placeholder="간단 설명 (선택)"
                        />
                    </div>
                </div>
            </article>

            <!-- 메타 정보 -->
            <article class="card p-16 meta">
                <h3 style="margin: 0 0 8px; font-weight: 800">메타 정보</h3>
                <div class="row">
                    <select class="select" id="category">
                        <option value="">카테고리 선택</option>
                        <option>한식</option>
                        <option>양식</option>
                        <option>중식</option>
                        <option>디저트</option>
                        <option>비건</option>
                    </select>
                    <input
                            class="input"
                            id="time"
                            type="number"
                            min="0"
                            placeholder="조리시간(분)"
                    />
                </div>
                <div class="row">
                    <select class="select" id="difficulty">
                        <option value="">난이도</option>
                        <option>쉬움</option>
                        <option>보통</option>
                        <option>어려움</option>
                    </select>

                    <!-- 공개 스위치 -->
                    <label class="switch">
                        <input id="isPublic" type="checkbox" checked />
                        <span>공개 (끄면 비공개/임시)</span>
                    </label>
                </div>
            </article>

            <!-- 재료 -->
            <article class="card p-16">
                <h3 style="margin: 0 0 8px; font-weight: 800">재료</h3>
                <div class="items" id="ingredients">
                    <!-- 행 예시 -->
                    <div class="item-row">
                        <input class="input" placeholder="예) 스파게티 면" />
                        <input class="input" placeholder="예) 200g" />
                        <button type="button" class="btn icon del">🗑</button>
                    </div>
                </div>
                <div style="margin-top: 8px">
                    <button type="button" class="btn ghost" id="addIng">+ 재료 추가</button>
                </div>
            </article>
            <!-- 태그 -->
            <article class="card p-16">
                <h3 style="margin: 0 0 8px; font-weight: 800">태그</h3>
                <div class="tag-input">
                    <input
                            class="input mb-12"
                            id="tagInput"
                            placeholder="태그 입력 후 Enter" />
                    <button type="button" class="btn ghost" id="addTagBtn">+ 추가</button>
                </div>
                <div class="tags" id="tagList"></div>
            </article>

            <!-- 하단 액션 -->
            <article class="card p-16">
                <div class="actions">
                    <button class="btn" type="button">취소</button>
                    <button class="btn" id="saveDraft" type="button">임시 저장</button>
                    <button class="btn primary" id="publish" type="button">발행</button>
                </div>
            </article>

            <!-- 조리 단계 (드래그 정렬) — 이미지 레시피 Pane -->
            <article class="card p-16" id="imagePane">
                <div
                        style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <h3 style="margin: 0; font-weight: 800">조리 단계</h3>
                    <button type="button" class="btn ghost" id="addStep">+ 추가</button>
                </div>
                <div class="steps" id="steps"></div>
            </article>

            <!-- 동영상 레시피 Pane -->
            <article class="card p-16 hidden" id="videoPane" aria-hidden="true">
                <h3 style="margin:0 0 12px; font-weight:800">동영상 레시피</h3>
                <div class="video-form">
                    <input class="input" id="videoUrl" placeholder="동영상 URL을 붙여넣기 (예: https://youtu.be/VIDEO_ID)" />
                    <div class="video-preview" id="videoPreviewWrap">
                        <div class="ratio-16x9">
                            <iframe id="videoPreview" title="동영상 미리보기" allowfullscreen referrerpolicy="strict-origin-when-cross-origin"></iframe>
                        </div>
                        <p class="muted small" id="videoHint">YouTube 링크는 자동으로 미리보기가 보여요. 기타 플랫폼은 임베드 허용 여부에 따라 미리보기가 제한될 수 있어요.</p>
                    </div>
                    <textarea class="input" id="videoText" placeholder="설명(예: 조리 포인트, 대체 재료, 주의사항 등)"></textarea>
                </div>
            </article>
        </main>



        <!-- 작성 가이드 / 사이드 -->
        <aside class="card p-16 help">
            <h3 style="margin: 0 0 8px; font-weight: 800">작성 가이드</h3>
            <p class="tip"><strong>제목은 핵심만</strong> (검색에 잘 걸리게)</p>
            <p class="tip">대표 이미지는 가로 비율(16:9)을 추천해요.</p>
            <p class="tip">
                재료와 단계는 <strong>+</strong> 버튼으로 자유롭게 추가/삭제!
            </p>
            <p class="tip">
                발행 전 <strong>임시저장</strong>으로 초안을 안전하게 보관.
            </p>
        </aside>
    </section>
    </form>
</div>


<!-- 기존 JS 로직 유지 -->
<script src="${pageContext.request.contextPath}/js/recipe-create-update.js"></script>

<!-- JSP에서 contextPath 전역 변수 내려주기 -->
<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>
</body>
</html>