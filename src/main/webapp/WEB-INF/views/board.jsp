<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>관리자 대시보드</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #1e1e1e;
            color: #ffffff;
        }

        h1, h2 {
            text-align: center;
        }

        .dashboard-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 40px;
        }

        /* KPI 카드 스타일 */
        .kpi-cards {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
            justify-content: center;
        }

        .card {
            background-color: #2c2c2c;
            border-radius: 10px;
            padding: 20px 30px;
            text-align: center;
            min-width: 180px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.3);
        }

        .card h3 {
            margin: 0;
            font-size: 16px;
            color: #bbbbbb;
        }

        .card p {
            margin: 10px 0 0;
            font-size: 28px;
            font-weight: bold;
        }

        .kibana-btn {
            margin-top: 10px;
            padding: 6px 12px;
            background-color: #4caf50;
            color: #fff;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

    </style>
</head>
<body>

<h1>📊 쉐프리드 관리자 대시보드</h1>

<div class="dashboard-container">

    <!-- 핵심 KPI 카드 -->
    <div class="kpi-cards">
        <div class="card">
            <h3>오늘 신규 글</h3>
            <p>${todayRecipes}</p>
        </div>
        <div class="card">
            <h3>미처리 신고</h3>
            <p>${pendingReports}</p>
        </div>
    </div>

    <button class="kibana-btn" onclick="window.open('http://localhost:5601/app/r/s/5nVnj','_blank')">
        Kibana로 자세히 보기
    </button>

    <h2>대시보드</h2>
    <iframe src="http://localhost:5601/app/r/s/UGlQT" height="800" width="1600"></iframe>

</div>
</body>
</html>
