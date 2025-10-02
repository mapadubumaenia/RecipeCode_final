<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>FAQ 관리</title>
    <link rel="stylesheet" href="/css/common.css">

    <style>
        .action-buttons { margin-top:8px; }
        .action-buttons button { margin-right:4px; }

        .filters { display:flex; gap:8px; margin-bottom:16px; margin-top: 16px;}
        .chip { padding:6px 12px; border-radius:8px; border:1px solid #ccc; cursor:pointer; }
        .chip.is-active { background:#111827; color:#fff; border-color:#111827; }
        .faq-item { margin-bottom:12px; border:1px solid #eee; border-radius:8px; padding:12px; }
        .faq-q { display:flex; justify-content:space-between; width:100%; background:none; border:none; cursor:pointer; font-weight:bold; }
        .faq-a { margin-top:8px; }
        .action-buttons { margin-top:8px; }
        .action-buttons button { margin-right:4px; }

    </style>
</head>
<body>
<div class="container">
    <!-- 헤더 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">FAQ 관리</h1>
        </div>
    </header>

    <section class="layout">
        <main>
            <!-- 검색/카테고리 -->
            <article class="card p-16 search mb-8">
                <div class="search-bar" style="display:flex; gap:8px; flex-wrap:wrap; align-items:center;">
                    <input id="faqSearch" class="input" placeholder="질문을 검색하세요 (예: 비밀번호, 공개 설정, 이미지 크기)" style="flex:1; min-width:200px; padding:8px 12px; border-radius:8px; border:1px solid #ccc;" />
                    <button id="btnClear" class="btn ghost" style="padding:8px 12px; border-radius:8px;">지우기</button>
                    <button class="btn primary" onclick="location.href='/faq/addition'" style="padding:8px 12px; border-radius:8px;">+ 새 FAQ 등록</button>
                </div>
                <div class="filters" aria-label="카테고리 필터">
                    <button class="chip is-active" data-cat="all">전체</button>
                    <button class="chip" data-cat="ACCOUNT">계정</button>
                    <button class="chip" data-cat="RECIPE">게시글/레시피</button>
                    <button class="chip" data-cat="FOLLOW">팔로잉/네트워크</button>
                    <button class="chip" data-cat="PERSONAL_INFO">개인정보/안전</button>
                    <button class="chip" data-cat="SEARCH">검색</button>
                    <button class="chip" data-cat="REPORT">신고/제재</button>
                </div>
                <div class="meta" id="matchCount">총 ${faqs.size()}개 질문</div>
            </article>

            <!-- FAQ 리스트 -->
            <section id="faqList" class="faq-list" aria-live="polite">
                <c:forEach var="faq" items="${faqs}" varStatus="st">
                    <article class="card faq-item" data-cat="${faq.faqTag}" id="q-${st.count}">
                        <button class="faq-q" aria-expanded="false" aria-controls="a${st.count}">
                            <span>${faq.faqQuestion}</span>
                            <span class="arrow">▶</span>
                        </button>
                        <div class="faq-a" id="a${st.count}" hidden>
                            <p>${faq.faqAnswer}</p>
                            <div class="action-buttons">
                                <button class="btn primary"
                                        onclick="location.href='/faq/edition?faqNum=${faq.faqNum}'">수정</button>
                                <form action="/faq/delete" method="post" style="display:inline">
                                    <input type="hidden" name="faqNum" value="${faq.faqNum}" />
                                    <button type="submit" class="btn danger"
                                            onclick="return confirm('정말 삭제하시겠습니까?')">삭제</button>
                                </form>

                            </div>
                        </div>
                    </article>
                </c:forEach>
            </section>
        </main>
    </section>
</div>

<script>
    // 아코디언 토글
    document.addEventListener('click', (e)=>{
        const btn = e.target.closest('.faq-q');
        if(!btn) return;
        const panel = document.getElementById(btn.getAttribute('aria-controls'));
        const expanded = btn.getAttribute('aria-expanded') === 'true';
        btn.setAttribute('aria-expanded', String(!expanded));
        panel.hidden = expanded;
    });

    // 검색/필터
    const searchInput = document.getElementById('faqSearch');
    const countEl = document.getElementById('matchCount');
    let activeCat = 'all';

    function filterFaq(){
        const q = (searchInput.value || '').trim().toLowerCase();
        let match = 0;
        document.querySelectorAll('.faq-item').forEach(item=>{
            const cat = item.dataset.cat || 'other';
            const text = item.textContent.toLowerCase();
            const passCat = activeCat === 'all' || cat === activeCat;
            const passQuery = !q || text.includes(q);
            const show = passCat && passQuery;
            item.classList.toggle('hidden', !show);
            if(show) match++;
        });
        countEl.textContent = (activeCat==='all' ? '총 ' : '') + match + '개 질문';
    }

    searchInput.addEventListener('input', filterFaq);
    document.getElementById('btnClear').addEventListener('click', ()=>{
        searchInput.value='';
        filterFaq();
        searchInput.focus();
    });

    document.querySelectorAll('.filters .chip').forEach(ch=>{
        ch.addEventListener('click', ()=>{
            document.querySelectorAll('.filters .chip').forEach(x=>x.classList.remove('is-active'));
            ch.classList.add('is-active');
            activeCat = ch.dataset.cat;
            filterFaq();
        });
    });

    // 질문열기
    function openFromHash(){
        if(!location.hash) return;
        const id = location.hash.slice(1);
        const item = document.getElementById(id);
        if(!item) return;
        const btn = item.querySelector('.faq-q');
        const panel = item.querySelector('.faq-a');
        if(btn && panel){ btn.setAttribute('aria-expanded','true'); panel.hidden=false; }
        item.scrollIntoView({behavior:'smooth', block:'start'});
    }
    window.addEventListener('hashchange', openFromHash);

    // 플로팅 버튼: 스크롤 300px 넘으면 노출
    const topBtn = document.getElementById('faqTop');
    window.addEventListener('scroll', ()=>{ topBtn.style.display = window.scrollY > 300 ? 'block' : 'none'; });
    topBtn.addEventListener('click', ()=> window.scrollTo({top:0, behavior:'smooth'}));

    // 초기화
    filterFaq();
    openFromHash();
</script>
</body>
</html>
