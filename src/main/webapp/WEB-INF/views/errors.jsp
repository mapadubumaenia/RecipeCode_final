<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <%@ include file="/WEB-INF/views/common/head.jsp" %>
  <title>에러 페이지</title>
  <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
  <link rel="stylesheet" href="<c:url value='/css/error.css'/>">
</head>
<body>
<main class="container">
  <section class="error-box">
    <!-- 에러 메시지 -->
    <h2 class="error-message">
      <c:out value="${errors}" />
    </h2>

    <!-- 버튼 영역 -->
    <div class="error-actions">
      <a href="/" class="btn btn-home">홈으로</a>
      <button type="button" class="btn btn-back" onclick="history.back()">이전 화면</button>
    </div>
  </section>
</main>
</body>
</html>
