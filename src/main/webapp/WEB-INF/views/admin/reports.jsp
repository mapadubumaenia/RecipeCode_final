<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>관리자 신고 페이지</title>
    <link rel="stylesheet" href="/css/common.css">

    <style>
        /* 카드 */
        .container-card {
            background: #fff;
            border-radius: 12px;
            padding: 24px;
            width: 100%;
            max-width: 1200px;
            margin: auto;
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }
        /* 제목 */
        .page-title {
            max-width: 1200px;
            margin: 32px auto 16px;
            font-size: 1.8rem;
            font-weight: bold;
        }
        /* 탭 */
        .tabs { display: flex; gap: 8px; margin-bottom: 24px; }
        .tabs button {
            padding: 10px 20px;
            border: 1px solid #ccc;
            border-radius: 8px;
            background: #fff;
            cursor: pointer;
            transition: 0.2s;
        }
        .tabs button.active {background: #111827;color: #fff;border-color: #111827;}
        .tabContent { display: none; }
        .tabContent.active { display: block; }
        .filter { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
        .filter select { padding: 6px 10px; border-radius: 6px; border: 1px solid #ccc; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; border-bottom: 1px solid #eee; text-align: left; }
        th { background: #f5f5f5; }
        .btn.small { padding: 5px 10px; border-radius: 8px; font-size: 0.85rem; }
        .btn.green { background: #22c55e; color: #fff; border: none; }
        .btn.red { background: #ef4444; color: #fff; border: none; }
        /* 사이드바 메뉴 크기/행간 고정 */
        .sidebar .nav a {
            font-size: 14px;
            line-height: 1.4;
            padding: 10px 12px;
        }
        .sidebar .brand {
            font-size: 14px;
        }
    </style>
</head>
<body>

<h1 class="page-title">신고/관리</h1>

<div class="container-card">
    <!-- 탭 -->
    <div class="tabs">
        <button class="active" data-tab="recipe">레시피 신고</button>
        <button data-tab="comment">댓글 신고</button>
    </div>

    <!-- 레시피 신고 탭 -->
    <div id="tabRecipe" class="tabContent active">
        <div class="filter">
            상태:
            <select id="recipeStatusFilter">
                <option value="">전체</option>
                <option value="1">처리중</option>
                <option value="2">완료</option>
            </select>
            유형:
            <select id="recipeTypeFilter">
                <option value="">전체</option>
                <option value="0">욕설</option>
                <option value="1">스팸</option>
                <option value="2">저작권</option>
            </select>
        </div>

        <table>
            <thead>
            <tr>
                <th>제목</th>
                <th>유형</th>
                <th>사유</th>
                <th>중복</th>
                <th>상태</th>
                <th>관리</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="r" items="${recipeReports}">
                <tr class="recipeRow" data-status="${r.reportStatus}" data-type="${r.reportType}">
                    <td><c:out value="${r.recipeTitle}"/></td>
                    <td>${r.reportType == 0 ? "욕설" : r.reportType == 1 ? "스팸" : "저작권"}</td>
                    <td><c:out value="${r.reason}"/></td>
                    <td><c:out value="${r.duplicateCount}"/></td>
                    <td>${r.reportStatus == 1 ? "처리중" : "완료"}</td>
                    <td>
                        <!-- 삭제 -->
                        <form action="/report/edit" method="post" style="display:inline;" onsubmit="return confirm('정말 삭제하시겠습니까?');">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <input type="hidden" name="reportId" value="${r.reportId}">
                            <input type="hidden" name="newStatus" value="2">
                            <input type="hidden" name="uuid" value="${r.uuid}">
                            <button type="submit" class="btn small red">삭제</button>
                        </form>

                        <!-- 유지 -->
                        <form action="/report/edit" method="post" style="display:inline;" onsubmit="return confirm('신고를 처리 완료하시겠습니까? (글은 유지됩니다)');">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <input type="hidden" name="reportId" value="${r.reportId}">
                            <input type="hidden" name="newStatus" value="2">
                            <button type="submit" class="btn small green">유지</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <!-- 댓글 신고 탭 -->
    <div id="tabComment" class="tabContent">
        <div class="filter">
            상태:
            <select id="commentStatusFilter">
                <option value="">전체</option>
                <option value="0">대기중</option>
                <option value="1">처리중</option>
                <option value="2">완료</option>
            </select>
            유형:
            <select id="commentTypeFilter">
                <option value="">전체</option>
                <option value="0">욕설</option>
                <option value="1">스팸</option>
                <option value="2">저작권</option>
            </select>
        </div>

        <table>
            <thead>
            <tr>
                <th>댓글 내용</th>
                <th>신고자</th>
                <th>유형</th>
                <th>사유</th>
                <th>중복</th>
                <th>상태</th>
                <th>관리</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="c" items="${commentReports}">
                <tr class="commentRow" data-status="${c.reportStatus}" data-type="${c.reportType}">
                    <td><c:out value="${c.commentContent}"/></td>
                    <td><c:out value="${c.userEmail}"/></td>
                    <td>${c.reportType == 0 ? "욕설" : c.reportType == 1 ? "스팸" : "저작권"}</td>
                    <td><c:out value="${c.reason}"/></td>
                    <td><c:out value="${c.duplicateCount}"/></td>
                    <td>${c.reportStatus == 0 ? "대기중" : c.reportStatus == 1 ? "처리중" : "완료"}</td>
                    <td>
                        <form action="/comments/report/updateStatus" method="post" style="display:inline;">
                            <input type="hidden" name="reportId" value="${c.reportId}">
                            <input type="hidden" name="adminEmail" value="${loginUserEmail}">
                            <select name="newReportStatus">
                                <option value="0" ${c.reportStatus == 0 ? 'selected' : ''}>대기중</option>
                                <option value="1" ${c.reportStatus == 1 ? 'selected' : ''}>처리중</option>
                                <option value="2" ${c.reportStatus == 2 ? 'selected' : ''}>완료</option>
                            </select>
                            <button type="submit" class="btn small green">변경</button>
                        </form>
                        <form action="/comments/report/delete" method="post" style="display:inline;">
                            <input type="hidden" name="reportId" value="${c.reportId}">
                            <button type="submit" class="btn small red" onclick="return confirm('정말 삭제하시겠습니까?')">삭제</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<script>
    // 탭 전환
    document.querySelectorAll('.tabs button').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.tabContent').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tabs button').forEach(b => b.classList.remove('active'));
            document.getElementById('tab' + this.dataset.tab.charAt(0).toUpperCase() + this.dataset.tab.slice(1))
                .classList.add('active');
            this.classList.add('active');
        });
    });

    // 필터 적용
    function applyFilter(rowSelector, statusFilterId, typeFilterId) {
        const status = document.getElementById(statusFilterId).value;
        const type = document.getElementById(typeFilterId).value;
        document.querySelectorAll(rowSelector).forEach(r => {
            const show = (!status || r.dataset.status === status) &&
                (!type || r.dataset.type === type);
            r.style.display = show ? '' : 'none';
        });
    }

    document.getElementById('recipeStatusFilter').addEventListener('change', () =>
        applyFilter('.recipeRow','recipeStatusFilter','recipeTypeFilter'));
    document.getElementById('recipeTypeFilter').addEventListener('change', () =>
        applyFilter('.recipeRow','recipeStatusFilter','recipeTypeFilter'));
    document.getElementById('commentStatusFilter').addEventListener('change', () =>
        applyFilter('.commentRow','commentStatusFilter','commentTypeFilter'));
    document.getElementById('commentTypeFilter').addEventListener('change', () =>
        applyFilter('.commentRow','commentStatusFilter','commentTypeFilter'));
</script>

</body>
</html>
