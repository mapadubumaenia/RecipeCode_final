<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>댓글 신고 관리</title>
</head>
<body>
<div class="card" id="tab-reports">
    <div class="hd">
        <h3>댓글 신고 현황</h3>

        <!-- 상태별 탭 -->
        <div class="tabs">
            <a href="/comments/report/list" class="${param.reportStatus == null ? 'active' : ''}">
                전체 (${page.totalElements})
            </a>
            <a href="/comments/report/list?reportStatus=0" class="${param.reportStatus == '0' ? 'active' : ''}">
                대기중 (${대기중})
            </a>
            <a href="/comments/report/list?reportStatus=1" class="${param.reportStatus == '1' ? 'active' : ''}">
                처리중 (${처리중})
            </a>
            <a href="/comments/report/list?reportStatus=2" class="${param.reportStatus == '2' ? 'active' : ''}">
                완료 (${완료})
            </a>
        </div>

        <!-- 신고 유형 필터 -->
        <div class="toolbar">
            <form method="get" action="/comments/report/list">
                <select name="reportType" class="input" onchange="this.form.submit()">
                    <option value="" ${empty param.reportType ? 'selected' : ''}>유형 전체</option>
                    <option value="0" ${param.reportType == '0' ? 'selected' : ''}>욕설</option>
                    <option value="1" ${param.reportType == '1' ? 'selected' : ''}>스팸</option>
                    <option value="2" ${param.reportType == '2' ? 'selected' : ''}>저작권</option>
                </select>
            </form>
        </div>
    </div>

    <div class="bd">
        <!-- 신고 목록 테이블 -->
        <table class="table">
            <thead>
            <tr>
                <th>댓글 내용</th>
                <th>신고자 이메일</th>
                <th>신고 유형</th>
                <th>신고 사유</th>
                <th>신고 횟수</th>
                <th>상태</th>
                <th>처리 관리자</th>
                <th class="right">처리</th>
            </tr>
            </thead>
            <tbody>
            <c:if test="${empty reports}">
                <tr>
                    <td colspan="8" class="empty-row">조건에 맞는 신고가 없습니다.</td>
                </tr>
            </c:if>

            <c:forEach var="report" items="${reports}">
                <tr>
                    <td>${report.commentContent}</td>
                    <td>${report.userEmail}</td>
                    <td>
                        <c:choose>
                            <c:when test="${report.reportType == 0}">욕설</c:when>
                            <c:when test="${report.reportType == 1}">스팸</c:when>
                            <c:when test="${report.reportType == 2}">저작권</c:when>
                            <c:otherwise>기타</c:otherwise>
                        </c:choose>
                    </td>
                    <td>${report.reason}</td>
                    <td>${report.duplicateCount}</td>
                    <td>
                        <c:choose>
                            <c:when test="${report.reportStatus == 0}">대기중</c:when>
                            <c:when test="${report.reportStatus == 1}">처리중</c:when>
                            <c:when test="${report.reportStatus == 2}">처리완료</c:when>
                        </c:choose>
                    </td>
                    <td>${report.adminEmail}</td>

                    <td class="right">
                        <!-- 상태 변경 -->
                        <form action="/comments/report/updateStatus" method="post" style="display:inline;">
                            <input type="hidden" name="reportId" value="${report.reportId}">
                            <input type="hidden" name="adminEmail" value="${loginUserEmail}">
                            <select name="newReportStatus">
                                <option value="0" ${report.reportStatus == 0 ? 'selected' : ''}>대기중</option>
                                <option value="1" ${report.reportStatus == 1 ? 'selected' : ''}>처리중</option>
                                <option value="2" ${report.reportStatus == 2 ? 'selected' : ''}>처리완료</option>
                            </select>
                            <button type="submit" class="btn small green">변경</button>
                        </form>

                        <!-- 삭제 -->
                        <form action="/comments/report/delete" method="post" style="display:inline;">
                            <input type="hidden" name="reportId" value="${report.reportId}">
                            <button type="submit" class="btn small red" onclick="return confirm('정말 삭제하시겠습니까?')">삭제
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <!-- 페이징 -->
        <div class="pagination">
            <c:if test="${page.totalPages > 1}">
                <c:forEach var="i" begin="0" end="${page.totalPages - 1}">
                    <a href="?page=${i}
                <c:if test='${not empty param.reportStatus}'> &reportStatus=${param.reportStatus}</c:if>
                <c:if test='${not empty param.reportType}'> &reportType=${param.reportType}</c:if>"
                       class="${i == page.number ? 'active' : ''}">
                            ${i + 1}
                    </a>
                </c:forEach>
            </c:if>
        </div>

    </div>
</div>

<script>
    document.querySelectorAll('.btn-update-status').forEach(btn => {
        btn.addEventListener('click', function () {
            const reportId = this.dataset.reportId;
            const select = document.querySelector('.status-select[data-report-id="' + reportId + '"]');
            const newStatus = select.value;

            fetch('/comments/report/updateStatus', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: `reportId=${reportId}&newReportStatus=${newStatus}&adminEmail=admin@example.com`
            })
                .then(res => {
                    if (res.ok) {
                        alert('상태가 변경되었습니다.');
                        location.reload(); // 새로고침 또는 테이블만 갱신
                    } else {
                        alert('변경 실패');
                    }
                });
        });
    });
</script>
</body>
</html>
