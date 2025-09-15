<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>FAQ 등록</title>
</head>
<body>
<h2>FAQ 등록</h2>

<form action="/faq/add" method="post">
    <label for="faq_question">질문</label><br>
    <input type="text" id="faq_question" name="faq_question" required><br><br>

    <label for="faq_answer">답변</label><br>
    <textarea id="faq_answer" name="faq_answer" rows="5" required></textarea><br><br>

    <label for="faq_tag">태그</label><br>
    <input type="text" id="faq_tag" name="faq_tag"><br><br>

    <button type="submit">저장</button>
</form>

<a href="/faq">목록으로</a>
</body>
</html>