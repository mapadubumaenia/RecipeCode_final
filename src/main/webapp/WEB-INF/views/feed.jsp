<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title>개인 추천</title>
  <style>
    body { font-family: system-ui, sans-serif; padding: 20px; }
    form { display:flex; gap:8px; align-items:center; margin-bottom:16px; }
    input, button { padding:8px; }
    .card { border:1px solid #ddd; padding:12px; margin:10px 0; border-radius:8px }
    .muted { color:#666; font-size:12px }
  </style>
</head>
<body>

<!-- 항상 보이는 폼 -->
<form method="get" action="/feed">
  <label>userId:
    <input type="text" name="userId" value="${userId}" placeholder="예: u100" required />
  </label>
  <label>size:
    <input type="number" name="size" value="${size}" min="1" max="50"/>
  </label>
  <button type="submit">열기</button>
  <c:if test="${not empty next}">
    <!-- 필요하면 after 커서를 그대로 이어서 보낼 수도 있음 -->
    <input type="hidden" name="after" value="${next}"/>
  </c:if>
</form>

<!-- 안내문구: userId 없을 때 -->
<c:if test="${not hasUser}">
  <p class="muted">상단에 <b>userId</b>를 입력하고 열기를 눌러주세요. (예: <a href="/feed?userId=u100">u100</a>)</p>
</c:if>

<!-- 결과 영역 -->
<c:choose>
  <c:when test="${hasUser and not empty items}">
    <h2><c:out value="${userId}"/> 님의 추천</h2>
    <c:forEach var="it" items="${items}">
      <div class="card">
        <h3><c:out value="${it.title}"/></h3>
        <p>작성자: <c:out value="${it.authorNick}"/></p>
        <p>좋아요: <c:out value="${it.likes}"/></p>
        <p>태그:
          <c:forEach var="t" items="${it.tags}">
            <span><c:out value="${t}"/></span>
          </c:forEach>
        </p>
        <p class="muted">
          ID: <c:out value="${it.id}"/> · recScore: <c:out value="${it.recScore}"/>
        </p>
      </div>
    </c:forEach>

    <c:if test="${not empty next}">
      <p><a href="/feed?userId=${userId}&after=${next}&size=${size}">더 보기</a></p>
    </c:if>
  </c:when>

  <c:when test="${hasUser and empty items}">
    <h2><c:out value="${userId}"/> 님의 추천</h2>
    <p>표시할 추천이 없습니다.</p>
  </c:when>
</c:choose>

</body>
</html>
