<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>FAQ</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/faq_all.css">
</head>
<body>
<div class="container">
    <!-- 헤더 -->
    <header class="profile-header">
        <div class="flex-row">
            <h1 class="page-title">FAQ</h1>
            <a href="/" class="float-text">home</a>
        </div>
        <a class="btn-logout" href="newfeed-ver-main-wireframe.html">Back</a>
    </header>

    <section class="layout">
        <!-- MAIN -->
        <main>
            <!-- 검색/카테고리 -->
            <article class="card p-16 search mb-8">
                <div class="row">
                    <input id="faqSearch" class="input" placeholder="질문을 검색하세요 (예: 비밀번호, 공개 설정, 이미지 크기)" />
                    <button id="btnClear" class="btn ghost">지우기</button>
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
                            <div class="help">
                                <span class="meta">도움이 되었나요?</span>
                                <button class="btn small" data-vote>예</button>
                                <button class="btn small ghost" data-vote>아니오</button>
                            </div>
                        </div>
                    </article>
                </c:forEach>

            </section>
        </main>

        <!-- SIDEBAR -->
        <aside class="sidebar">
            <article class="card p-16 side-card">
                <h3 style="margin:0">바로가기</h3>
                <nav class="toc" aria-label="FAQ 바로가기">
                    <c:forEach var="faq" items="${faqs}" varStatus="st">
                        <a href="#q-${st.count}">
                                ${faq.faqQuestion}
                        </a>
                    </c:forEach>
                </nav>
            </article>

            <article class="card p-16 side-card" style="margin-top:12px;">
                <h3 style="margin:0">문의가 필요해요?</h3>
                <p class="meta">FAQ로 해결되지 않으면 아래로 알려주세요.</p>
                <a class="btn primary" href="mailto:support@example.com">이메일로 문의</a>
            </article>
        </aside>
    </section>
</div>

<!-- 위로가기 버튼 -->
<button id="faqTop" class="faq-btn" aria-label="맨 위로">top</button>
<%--카테고리별 조회용--%>
<script>
    const filterButtons = document.querySelectorAll(".filters .chip");
    const faqItems = document.querySelectorAll("#faqList .faq-item");
    const matchCount = document.getElementById("matchCount");

    filterButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            // 선택된 버튼 강조
            filterButtons.forEach((b) => b.classList.remove("is-active"));
            btn.classList.add("is-active");

            const cat = btn.dataset.cat;
            let visibleCount = 0;

            faqItems.forEach((item) => {
                if (cat === "all" || item.dataset.cat === cat) {
                    item.style.display = "";   // 보이기
                    visibleCount++;
                } else {
                    item.style.display = "none"; // 숨기기
                }
            });

            // 개수 업데이트
            matchCount.textContent = `총 ${visibleCount}개 질문`;
        });
    });
</script>


<script>
    const $$ = (s, el=document) => [...el.querySelectorAll(s)];
    const $ = (s, el=document) => el.querySelector(s);

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
    const searchInput = $('#faqSearch');
    const countEl = $('#matchCount');
    let activeCat = 'all';

    function filterFaq(){
        const q = (searchInput.value || '').trim().toLowerCase();
        let match = 0;
        $$('.faq-item').forEach(item=>{
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
    $('#btnClear').addEventListener('click', ()=>{ searchInput.value=''; filterFaq(); searchInput.focus(); });

    $$('.filters .chip').forEach(ch=>{
        ch.addEventListener('click', ()=>{
            $$('.filters .chip').forEach(x=>x.classList.remove('is-active'));
            ch.classList.add('is-active');
            activeCat = ch.dataset.cat;
            filterFaq();
        });
    });

    // 해시로 특정 질문 열기
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
    const topBtn = $('#faqTop');
    window.addEventListener('scroll', ()=>{
        topBtn.style.display = window.scrollY > 300 ? 'block' : 'none';
    });
    topBtn.addEventListener('click', ()=> window.scrollTo({top:0, behavior:'smooth'}));

    // 초기화
    filterFaq();
    openFromHash();
</script>
</body>
</html>
