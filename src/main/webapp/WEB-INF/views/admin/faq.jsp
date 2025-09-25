<%@ page contentType="text/html; charset=UTF-8" %>
<h1 style="margin:0 0 12px">FAQ 관리</h1>

<section class="card">
    <div style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
        <button class="btn primary" id="faqAdd">+ 새 항목</button>
    </div>
    <div id="faqErr" class="error"></div>
    <table style="width:100%;border-collapse:collapse;background:#fff">
        <thead><tr><th>#</th><th>질문</th><th>노출</th><th>정렬</th><th>액션</th></tr></thead>
        <tbody id="faqTbody"></tbody>
    </table>
</section>

<!-- page script -->
<script src="<c:url value='/js/faq.js'/>" defer></script>
