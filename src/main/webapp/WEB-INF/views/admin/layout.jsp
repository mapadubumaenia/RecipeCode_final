<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>Admin · Recipe Code</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="<c:url value='/css/common.css'/>">
  <style>
    :root{--side:#0f172a;--ink:#cbd5e1;--active:#22c55e;--line:#e5e7eb;--bg:#f8fafc}
    *{box-sizing:border-box}
    body{margin:0;background:var(--bg);font:14px/1.45 system-ui,-apple-system,Segoe UI,Roboto,'Noto Sans KR',Arial}
    .shell{display:grid;grid-template-columns:260px 1fr;min-height:100vh}
    .sidebar{background:var(--side);color:var(--ink);padding:12px}
    .brand{color:#fff;font-weight:800;padding:10px 12px}
    .nav-group{margin:14px 8px 6px;font-size:12px;opacity:.8;text-transform:uppercase}
    .nav a{display:flex;align-items:center;gap:10px;padding:10px 12px;color:var(--ink);text-decoration:none;border-radius:8px}
    .nav a:hover{background:rgba(255,255,255,.06)}
    .nav a.active{background:rgba(34,197,94,.16);color:#fff;box-shadow:inset 0 0 0 1px rgba(34,197,94,.5)}
    .content{display:flex;flex-direction:column}
    .topbar{background:#fff;border-bottom:1px solid var(--line)}
    .topbar .row{max-width:1120px;margin:0 auto;padding:10px 16px;display:flex;gap:8px;align-items:center}
    .grow{flex:1}
    .container{max-width:1120px;margin:0 auto;padding:18px 16px}
    .card{background:#fff;border:1px solid var(--line);border-radius:12px;padding:14px}
    .muted{color:#6b7280}
  </style>

  <!-- 전역 레이아웃 JS -->
  <c:url var="layoutJs" value="/js/layout.js"/>
  <script src="${layoutJs}" defer></script>
</head>
<body>
<div class="shell">
  <!-- left -->
  <aside class="sidebar">
    <div class="brand">🟢 Recipe Admin</div>
    <nav class="nav">
      <a href="<c:url value='/admin'/>" data-path="/admin">📊 대시보드</a>
      <a href="<c:url value='/admin/analytics'/>" data-path="/admin/analytics">📈 차트</a>
      <a href="<c:url value='/admin/moderation/reports'/>" data-path="/admin/moderation/reports">🚨 신고/관리</a>
      <a href="<c:url value='/admin/faq'/>" data-path="/admin/faq">❓ FAQ</a>
    </nav>
  </aside>

  <!-- right -->
  <section class="content">
    <div class="topbar">
      <div class="row">
        <div class="grow">
        </div>
        <sec:authorize access="isAuthenticated()">
          <span class="muted"><sec:authentication property="principal.nickname"/> 님</span>
        </sec:authorize>
      </div>
    </div>

    <!-- DEBUG: ctx=[<c:out value='${pageContext.request.contextPath}'/>]
     uri=[<c:out value='${pageContext.request.requestURI}'/>]
     path=[<c:out value='${fn:substring(pageContext.request.requestURI, fn:length(pageContext.request.contextPath), fn:length(pageContext.request.requestURI))}'/>] -->

    <main class="container" id="adminView">

      <c:set var="fwdUri" value="${requestScope['jakarta.servlet.forward.request_uri']}" />
      <c:set var="fwdCtx" value="${requestScope['jakarta.servlet.forward.context_path']}" />
      <c:set var="uri" value="${empty fwdUri ? pageContext.request.requestURI : fwdUri}" />
      <c:set var="ctx" value="${empty fwdCtx ? pageContext.request.contextPath : fwdCtx}" />
      <c:set var="path" value="${fn:substring(uri, fn:length(ctx), fn:length(uri))}" />


      <c:choose>
        <c:when test="${path eq '/admin'}">
          <jsp:include page="/WEB-INF/views/admin/overview.jsp"/>
        </c:when>

        <c:when test="${fn:startsWith(path, '/admin/analytics')}">
          <jsp:include page="/WEB-INF/views/admin/analytics.jsp"/>
        </c:when>

        <c:when test="${fn:startsWith(path, '/admin/moderation/reports')}">
          <jsp:include page="/WEB-INF/views/admin/reports.jsp"/>
        </c:when>

        <c:when test="${fn:startsWith(path, '/admin/faq')}">
          <jsp:include page="/WEB-INF/views/admin/faq.jsp"/>
        </c:when>


        <c:otherwise>
          <jsp:include page="/WEB-INF/views/admin/overview.jsp"/>
        </c:otherwise>
      </c:choose>
    </main>
  </section>
</div>
</body>
</html>
