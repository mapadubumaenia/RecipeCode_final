<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>Admin Analytics</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Chart.js -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
  <style>
    body{font-family:system-ui, -apple-system, Segoe UI, Roboto, 'Noto Sans KR', Arial, sans-serif; margin:20px;}
    h1{margin:0 0 8px}
    h2{margin:28px 0 8px}
    .grid{display:grid; gap:16px}
    @media(min-width:1100px){ .grid{grid-template-columns:1fr 1fr} }
    table{width:100%; border-collapse:collapse; background:#fff}
    th,td{border-bottom:1px solid #eee; padding:8px 10px; text-align:left}
    th{background:#fafafa}
    .controls{display:flex; gap:8px; align-items:center; margin:8px 0 12px}
    .chip{display:inline-block; padding:4px 10px; border-radius:999px; background:#f3f4f6; margin:2px 4px 2px 0}
    .card{background:#fff; border:1px solid #eee; border-radius:12px; padding:12px}
    .muted{color:#666; font-size:12px}
    .btn{padding:6px 12px; border-radius:8px; border:1px solid #ddd; background:#fff; cursor:pointer}
    .btn.primary{background:#111827; color:#fff; border-color:#111827}
    .right{float:right}
    .error{color:#b91c1c}
  </style>
</head>
<body>
<h1>관리자 대시보드</h1>
<div class="muted">/api/admin/analytics/* 엔드포인트 기반</div>

<section class="card">
  <h2>제로결과 키워드 Top N</h2>
  <div class="controls">
    <label>From: <input type="datetime-local" id="zkFrom"></label>
    <label>To: <input type="datetime-local" id="zkTo"></label>
    <label>Size: <input type="number" id="zkSize" value="20" min="1" max="100" style="width:80px"></label>
    <button class="btn primary" id="zkLoad">불러오기</button>
  </div>
  <div class="muted">클릭 시 해당 키워드로 검색 페이지 이동</div>
  <div id="zkErr" class="error"></div>
  <table>
    <thead><tr><th>#</th><th>키워드</th><th>발생수</th><th>최근 발생</th></tr></thead>
    <tbody id="zkTbody"></tbody>
  </table>
</section>

<div class="grid">
  <section class="card">
    <h2>최근 많이 본 게시물 (Views)</h2>
    <div class="controls">
      <label>최근 일수: <input type="number" id="tvDays" value="7" min="1" max="90" style="width:80px"></label>
      <label>개수: <input type="number" id="tvSize" value="10" min="1" max="50" style="width:80px"></label>
      <button class="btn" id="tvLoad">불러오기</button>
    </div>
    <div id="tvErr" class="error"></div>
    <table>
      <thead><tr><th>#</th><th>제목</th><th>작성자</th><th>조회수</th><th>좋아요</th><th>업로드일</th></tr></thead>
      <tbody id="tvTbody"></tbody>
    </table>
  </section>

  <section class="card">
    <h2>최근 좋아요 많이 받은 게시물 (Likes)</h2>
    <div class="controls">
      <label>최근 일수: <input type="number" id="tlDays" value="7" min="1" max="90" style="width:80px"></label>
      <label>개수: <input type="number" id="tlSize" value="10" min="1" max="50" style="width:80px"></label>
      <button class="btn" id="tlLoad">불러오기</button>
    </div>
    <div id="tlErr" class="error"></div>
    <table>
      <thead><tr><th>#</th><th>제목</th><th>작성자</th><th>좋아요</th><th>조회수</th><th>업로드일</th></tr></thead>
      <tbody id="tlTbody"></tbody>
    </table>
  </section>
</div>

<div class="grid">
  <section class="card">
    <h2>트렌딩 태그 (최근 업로드 기준)</h2>
    <div class="controls">
      <label>최근 일수: <input type="number" id="ttDays" value="30" min="1" max="365" style="width:80px"></label>
      <label>개수: <input type="number" id="ttSize" value="20" min="1" max="100" style="width:80px"></label>
      <button class="btn" id="ttLoad">불러오기</button>
    </div>
    <div id="ttErr" class="error"></div>
    <div id="ttWrap"></div>
  </section>

  <section class="card">
    <h2>상위 크리에이터</h2>
    <div class="controls">
      <label>최근 일수: <input type="number" id="tcDays" value="30" min="1" max="365" style="width:80px"></label>
      <label>개수: <input type="number" id="tcSize" value="10" min="1" max="50" style="width:80px"></label>
      <button class="btn" id="tcLoad">불러오기</button>
    </div>
    <div id="tcErr" class="error"></div>
    <table>
      <thead><tr><th>#</th><th>작성자</th><th>게시물수</th><th>합산 좋아요</th><th>합산 조회수</th></tr></thead>
      <tbody id="tcTbody"></tbody>
    </table>
  </section>
</div>

<section class="card">
  <h2>일자별 신규 업로드 (최근 N일)</h2>
  <div class="controls">
    <label>최근 일수: <input type="number" id="udDays" value="30" min="1" max="365" style="width:80px"></label>
    <button class="btn" id="udLoad">불러오기</button>
  </div>
  <div id="udErr" class="error"></div>
  <canvas id="udChart" height="110"></canvas>
</section>

<!-- 컨텍스트 경로 전역 주입 -->
<script>
  window.APP_CTX = '${pageContext.request.contextPath}';
</script>
<!-- 외부 JS (정적 리소스 경로에 맞게) -->
<script src="${pageContext.request.contextPath}/js/admin-analytics.js"></script>
</body>
</html>
