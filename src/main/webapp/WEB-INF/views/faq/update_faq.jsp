<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>FAQ 수정/삭제</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/update_faq.css">
</head>
<body>

<div class="card">
    <div class="hd">
        <h3>FAQ 수정/삭제</h3>
    </div>
    <div class="bd">
        <form id="faqForm" name="faqForm" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input type="hidden" name="faqNum" value="<c:out value='${faq.faqNum}'/>">

            <div class="mb-12">
                <label for="faqQuestion">질문</label>
                <input type="text" id="faqQuestion" name="faqQuestion"
                       class="input"
                       value="<c:out value='${faq.faqQuestion}'/>" required/>
            </div>

            <div class="mb-12">
                <label for="faqAnswer">답변</label>
                <textarea id="faqAnswer" name="faqAnswer" rows="5"
                          class="input" required><c:out value='${faq.faqAnswer}'/></textarea>
            </div>

            <div class="mb-12">
                <label for="faqTag">태그</label>
                <input type="text" id="faqTag" name="faqTag"
                       class="input"
                       value="<c:out value='${faq.faqTag}'/>"/>
            </div>

            <div class="actions">
                <button type="button" class="btn primary" onclick="fn_save()">수정</button>
                <button type="button" class="btn danger" onclick="fn_delete()">삭제</button>
            </div>
        </form>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<script>
    function fn_save() {
        $("#faqForm").attr("action", "/faq/edit").submit();
    }

    function fn_delete() {
        if (confirm("정말 삭제하시겠습니까?")) {
            $("#faqForm").attr("action", "/faq/delete").submit();
        }
    }
</script>

</body>
</html>