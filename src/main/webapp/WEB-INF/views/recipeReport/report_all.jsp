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
            <div class="actions">
                <span class="chip">미처리 ${pages.totalElements}</span>
                <span class="chip">SLA 24h</span>
            </div>
        </div>

        <div class="bd">
            <!-- 필터/정렬 -->
            <div class="toolbar">
                <select class="input">
                    <option>유형 전체</option>
                    <option value="1">스팸</option>
                    <option value="0">욕설</option>
                    <option value="2">저작권</option>
                </select>
                <select class="input">
                    <option>정렬: 최신</option>
                    <option>정렬: 중복 많은순</option>
                </select>
                <label class="chip"><input type="checkbox"/> 증거 스냅샷 보기</label>
            </div>

            <!-- 신고 목록 -->
            <table class="table">
                <thead>
                <tr>
                    <th>대상</th>
                    <th>유형</th>
                    <th>사유</th>
                    <th>중복</th>
                    <th>상태</th>
                    <th class="right">처리</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="report" items="${reports}">
                    <tr>
                        <td>
<%--                            <a href="/recipes/detail?uuid=${report.recipes.uuid}">--%>
<%--                                <c:out value="${report.recipes.recipeTitle}"/>--%>
                            </a>
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
                        <td>5</td> <!-- 중복 카운트 예시, 추후 countByRecipesUuid() 연결 -->
                        <td>
                            <c:choose>
                                <c:when test="${report.reportStatus == 0}">
                                    <span class="status pending">미처리</span>
                                </c:when>
                                <c:when test="${report.reportStatus == 1}">
                                    <span class="status processing">처리중</span>
                                </c:when>
                                <c:when test="${report.reportStatus == 2}">
                                    <span class="status done">완료</span>
                                </c:when>
                            </c:choose>
                        </td>
                        <td class="right">
                            <!-- 차단 -->
                            <form action="/report/edit" method="post" style="display:inline;">
                                <input type="hidden" name="reportId" value="${report.reportId}">
                                <input type="hidden" name="newStatus" value="1">
                                <button type="submit" class="btn small red">차단</button>
                            </form>

                            <!-- 완료 -->
                            <form action="/report/edit" method="post" style="display:inline;">
                                <input type="hidden" name="reportId" value="${report.reportId}">
                                <input type="hidden" name="newStatus" value="2">
                                <button type="submit" class="btn small green">완료</button>
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
