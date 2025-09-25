<%@ page contentType="text/html; charset=UTF-8" %>
<h1 style="margin:0 0 12px">신고/관리</h1>

<section class="card">
  <div class="controls" style="display:flex;gap:8px;align-items:center">
    <label>기간 <input type="date" id="rpFrom"> ~ <input type="date" id="rpTo"></label>
    <label>유형
      <select id="rpType">
        <option value="">전체</option><option value="spam">스팸</option>
        <option value="abuse">욕설/혐오</option><option value="nsfw">부적절</option>
      </select>
    </label>
    <label>상태
      <select id="rpState">
        <option value="">전체</option><option value="OPEN">미처리</option>
        <option value="IN_PROGRESS">처리중</option><option value="RESOLVED">완료</option>
      </select>
    </label>
    <button class="btn" id="rpLoad">불러오기</button>
  </div>

  <div id="rpErr" class="error"></div>
  <table style="width:100%;border-collapse:collapse;background:#fff">
    <thead><tr><th>#</th><th>대상</th><th>유형</th><th>신고자</th><th>사유</th><th>상태</th><th>접수일</th><th>액션</th></tr></thead>
    <tbody id="rpTbody"><tr><td colspan="8" class="muted">데이터 없음</td></tr></tbody>
  </table>
</section>

<!-- page script -->
<script src="<c:url value='/js/reports.js'/>" defer></script>
