<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>FAQ 등록</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/add_faq.css">
</head>
<body>
<div class="card">
    <div class="hd">
        <h3>FAQ 등록</h3>
    </div>
    <div class="bd">
        <form action="/faq/add" method="post" class="faq-form">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

            <div class="mb-12">
                <label for="faqQuestion">질문</label><br/>
                <input type="text" id="faqQuestion" name="faqQuestion" class="input" required />
            </div>

            <div class="mb-12">
                <label for="faqAnswer">답변</label><br/>
                <textarea id="faqAnswer" name="faqAnswer" rows="5" class="input" required></textarea>
            </div>

            <div class="mb-12">
                <label for="faqTag">태그</label><br/>
                <input type="text" id="faqTag" name="faqTag" class="input" />
            </div>

            <div class="actions">
                <button type="submit" class="btn primary">저장</button>
                <button type="button" class="btn primary" onclick="location.href='/admin/faq'">목록</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
