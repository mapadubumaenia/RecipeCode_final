<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>신고 관리</title>
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/report.css">
</head>
<body>
<section id="tab-reports" role="tabpanel">
    <div class="card">
        <div class="hd">
            <h3>이용자 신고</h3>
            <!-- 탭 -->
            <div class="tabs">
                <a href="/report" class="${param.status == null ? 'active' : ''}">
                    전체 (${pages.totalElements})
                </a>
                <a href="/report?status=1" class="${param.status == '1' ? 'active' : ''}">
                    처리중 (${processingCount})
                </a>
                <a href="/report?status=2" class="${param.status == '2' ? 'active' : ''}">
                    처리완료 (${doneCount})
                </a>
            </div>
        </div>

        <div class="bd">
            <!-- 필터/정렬 -->
            <div class="toolbar">
                <form method="get" action="/report">
                    <select name="reportType" class="input" onchange="this.form.submit()">
                        <option value="" ${empty reportType ? 'selected' : ''}>유형 전체</option>
                        <option value="0" ${reportType != null and reportType == 0 ? 'selected' : ''}>욕설</option>
                        <option value="1" ${reportType != null and reportType == 1 ? 'selected' : ''}>스팸</option>
                        <option value="2" ${reportType != null and reportType == 2 ? 'selected' : ''}>저작권</option>
                    </select>
                </form>
            </div>

            <!-- 신고 목록 -->
            <table class="table">
                <thead>
                <tr>
                    <th>대상 게시글</th>
                    <th>신고 유형</th>
                    <th>신고 사유</th>
                    <th>신고 횟수</th>
                    <th>처리 상태</th>
                    <th class="right">처리</th>
                </tr>
                </thead>
                <tbody>
                <!-- 리스트가 비었을 때 -->
                <c:if test="${empty reports}">
                    <tr>
                        <td colspan="6" class="empty-row">
                            조건에 맞는 신고가 없습니다.
                        </td>
                    </tr>
                </c:if>

                <!-- 리스트가 있을 때 -->
                <c:forEach var="report" items="${reports}">
                    <tr>
                        <td>
                            <c:choose>
                                <c:when test="${report.recipeTitle eq '[삭제된 레시피]'}">
                                    <span class="deleted-title">[삭제된 레시피]</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="/recipes/${report.uuid}">
                                        <c:out value="${report.recipeTitle}"/>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${report.reportType == 0}">욕설</c:when>
                                <c:when test="${report.reportType == 1}">스팸</c:when>
                                <c:when test="${report.reportType == 2}">저작권</c:when>
                                <c:otherwise>기타</c:otherwise>
                            </c:choose>
                        </td>
                        <td class="muted">${report.reason}</td>
                        <td>${report.duplicateCount}</td>
                        <td>
                            <c:choose>
                                <c:when test="${report.reportStatus == 1}">
                                    <c:choose>
                                        <c:when test="${report.remainingHours > 0}">
                                            처리중 (${report.remainingHours}시간 남음)
                                        </c:when>
                                        <c:otherwise>
                                            처리중 (24시간 초과)
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:when test="${report.reportStatus == 2}">
                                    처리완료
                                </c:when>
                            </c:choose>
                        </td>

                        <td class="right">
                            <!-- 삭제 -->
                            <form action="/report/edit" method="post" style="display:inline;"
                                  onsubmit="return confirm('정말 해당 글을 삭제하시겠습니까?');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="reportId" value="${report.reportId}">
                                <input type="hidden" name="newStatus" value="2">
                                <input type="hidden" name="uuid" value="${report.uuid}">
                                <button type="submit" class="btn small red">삭제</button>
                            </form>


                            <!-- 유지 -->
                            <form action="/report/edit" method="post" style="display:inline;"
                                  onsubmit="return confirm('신고를 처리 완료하시겠습니까? (글은 유지됩니다)');">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <input type="hidden" name="reportId" value="${report.reportId}">
                                <input type="hidden" name="newStatus" value="2">
                                <button type="submit" class="btn small green">유지</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <!-- 페이징 -->
            <div class="pagination">
                <c:if test="${pages.totalPages > 1}">
                    <c:forEach var="i" begin="0" end="${pages.totalPages-1}">
                        <a href="?page=${i}" class="${i == pages.number ? 'active' : ''}">${i+1}</a>
                    </c:forEach>
                </c:if>
            </div>
        </div>
    </div>
</section>
</body>
</html>
