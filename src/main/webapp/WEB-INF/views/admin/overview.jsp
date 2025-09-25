<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="overviewJs" value="/js/overview.js"/>
<script src="${overviewJs}" defer></script>
<style>
  .kpis{display:grid;gap:12px;grid-template-columns:repeat(4,minmax(0,1fr))}
  .kpi{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:14px}
  .kpi .h{font-size:12px;color:#6b7280}
  .kpi .v{font-size:22px;font-weight:700;margin-top:6px}
  @media(max-width:900px){.kpis{grid-template-columns:repeat(2,1fr)}}
</style>

<h1 style="margin:0 0 12px">Overview</h1>
<div class="kpis">
  <div class="kpi"><div class="h">신규 업로드(7d)</div><div id="kpiUploads" class="v">-</div></div>
  <div class="kpi"><div class="h">총 조회(7d)</div><div id="kpiViews" class="v">-</div></div>
  <div class="kpi"><div class="h">검색 0건(7d)</div><div id="kpiZero" class="v">-</div></div>
  <div class="kpi"><div class="h">미처리 신고</div><div id="kpiReports" class="v">-</div></div>
</div>

<section class="card">
  <h2 style="margin:0 0 8px">최근 24시간 트래픽(검색 이벤트)</h2>
  <div class="muted">호버하면 시각·건수를 표시합니다</div>

  <div id="sparkWrap" class="spark-wrap">
    <canvas id="spark" height="110"></canvas>
    <div id="sparkTip" class="spark-tip" hidden></div>
  </div>
</section>

<style>
  .spark-wrap{ position:relative; width:100%; }
  #spark{ width:100%; display:block; }
  .spark-tip{
    position:absolute; pointer-events:none; padding:6px 8px; border-radius:6px;
    background:#111827; color:#fff; font-size:12px; box-shadow:0 4px 14px rgba(0,0,0,.25);
    transform:translate(-50%,-120%); white-space:nowrap; z-index:2;
  }
  .spark-dot{
    position:absolute; width:8px; height:8px; border-radius:50%; background:#111827; transform:translate(-50%,-50%);
    box-shadow:0 0 0 3px rgba(17,24,39,.12);
  }
</style>



<div style="margin-top:18px" class="card">
  <h2 style="margin:0 0 10px">최근 동향</h2>
  <div class="muted">차트 상세는 “차트” 메뉴에서 확인</div>
</div>

<!-- page script -->
<script src="<c:url value='/js/overview.js'/>" defer></script>
