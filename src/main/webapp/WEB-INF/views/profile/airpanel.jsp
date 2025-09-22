<%--
  Created by IntelliJ IDEA.
  User: user
  Date: 25. 9. 22.
  Time: 오전 8:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
</head>
<body>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="panel" id="airBox" data-ctx="${ctx}">
    <h3>🌫️오늘의 피크닉 지수</h3>

    <div class="air-row">
        <label for="sido">지역</label>
        <select id="sido" class="air-select">
            <option>서울</option><option selected>부산</option><option>대구</option>
            <option>인천</option><option>광주</option><option>대전</option>
            <option>울산</option><option>세종</option><option>경기</option>
            <option>강원</option><option>충북</option><option>충남</option>
            <option>전북</option><option>전남</option><option>경북</option>
            <option>경남</option><option>제주</option>
        </select>
    </div>

    <div id="airText" class="air-text">불러오는 중…</div>
</section>


<script>
    const ctx = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/mypage/airpanle.js"></script>
</body>
</html>
