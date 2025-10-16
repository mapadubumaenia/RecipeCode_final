$(function () {
    const notifBtn = $("#btnNotif");
    const notifPanel = $("#notifPanel");
    const notifList = $("#notifList");
    const notifDot = $(".notif-dot");
    const markAllBtn = $("#markAll");
    const closeBtn = $("#closeNotif");
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    
    // 알림 점 표시 업데이트
    function updateDot(hasUnread) {
        notifBtn.toggleClass("has-new", hasUnread);
    }

    // 안 읽은 알림 개수 불러오기
    function loadUnreadCount() {
        $.get("/api/notification/unreadCount", function (count) {
            updateDot(count > 0);
        });
    }

    // 알림 목록 불러오기
    function loadNotifications() {
        $.get("/api/notification", function (list) {
            notifList.empty();

            if (!list || list.length === 0) {
                notifList.html('<div class="notif-empty">새 알림이 없습니다.</div>');
                updateDot(false);
                return;
            }

            list.forEach(item => {
                const targetUrl = linkOf(item.notification); // 알림별 이동 링크
                // 신고결과 전용 라벨 제어: 삭제면 이동 없이 "확인", 유지면 링크 있으면 "열기"
                const isReportResult = item.notification && item.notification.event === "RECIPE_REPORT_RESULT";
                const isDeleted = isReportResult && (item.notification.message || "").includes("삭제");
                const buttonText = !item.read
                    ? "읽음"
                    : (targetUrl ? "열기" : (isReportResult ? (isDeleted ? "확인" : "열기") : "확인"));

                const el = $(`
                    <div class="notif-item ${item.read ? 'read' : 'unread'}" data-id="${item.deliveryId}">
                        <div class="ic">${iconOf(item.notification.event)}</div>
                        <div>
                            <div class="msg">${item.notification.message}</div>
                            <div class="time">${timeAgo(item.notification.insertTime)}</div>
                        </div>
                        <div>
                            <button type = "button"
                                    class="small" 
                                    data-read="${item.deliveryId}" 
                                    data-link="${targetUrl}">
                                ${buttonText}
                            </button>
                        </div>
                    </div>
                `);
                notifList.append(el);
            });

            const hasUnread = list.some(n => !n.read);
            updateDot(hasUnread);
        });
    }

    function linkOf(notification) {
        switch (notification.sourceType) {
            case "COMMENT":
            case "LIKE":
                return `/recipes/${notification.recipeUuid}`;
            case "FOLLOW":
                return `/profile/${notification.actorUserId}`;
            case "RECIPE": // 유지 케이스: sourceId에 recipeUuid가 들어옴
                return notification.sourceId ? `/recipes/${notification.sourceId}` : "";
            default:
                // 신고 결과
                if (notification.event === "RECIPE_REPORT_RESULT") {
                    const msg = (notification.message || "");
                    const isDeleted = msg.includes("삭제");
                    if (isDeleted) return ""; // 삭제면 이동 없음
                    // 유지면 레시피로 이동
                    if (notification.recipeUuid) return `/recipes/${notification.recipeUuid}`;
                    // recipeUuid 없으면 이동 생략
                    return "";
                }
                return "/";
        }
    }

    // 이벤트 → 아이콘 매핑
    function iconOf(type) {
        switch (type) {
            case "COMMENT":
                return "💬";
            case "LIKE":
                return "❤️";
            case "FOLLOW":
                return "👥";
            case "RECIPE_REPORT_RESULT":
                return "🚩";
            default:
                return "🔔";
        }
    }

    // 패널 열기/닫기
    function openPanel() {
        notifPanel.addClass("open");
        notifBtn.attr("aria-expanded", "true");
        loadNotifications();
    }

    function closePanel() {
        notifPanel.removeClass("open");
        notifBtn.attr("aria-expanded", "false");
    }

    function togglePanel() {
        notifPanel.hasClass("open") ? closePanel() : openPanel();
    }

    // 이벤트 바인딩
    notifBtn.on("click", function (e) {
        e.stopPropagation();
        togglePanel();
    });

    closeBtn.on("click", closePanel);

    $(document).on("click", function (e) {
        if (!notifPanel.is(e.target) && notifPanel.has(e.target).length === 0 && !notifBtn.is(e.target)) {
            closePanel();
        }
    });

    $(window).on("keydown", function (e) {
        if (e.key === "Escape") closePanel();
    });

    // 개별 알림 읽음 처리 + 이동
    // notifList.on("click", ".notif-item", function (e) {
    //     const button = $(this).find("button[data-read]");
    //     const deliveryId = button.data("read");
    //     const link = button.data("link");
    //     $.ajax({
    //         url: `/api/notification/${deliveryId}/read`,
    //         type: "PATCH",
    //         success: function () {
    //             console.log("PATCH 성공, 이동 시도:", link);
    //             if (link) {
    //                 window.location.href = link;  // 정상 이동
    //             } else {
    //                 loadNotifications();
    //                 loadUnreadCount();
    //             }
    //         },
    //         error: function (xhr, status, error) {
    //             console.error("PATCH 실패:", status, error);
    //         }
    //     });
    // });

    // 개별 버튼 동작: "읽음"이면 읽음 처리만, "열기"면 링크 이동
    notifList.on("click", "button[data-read]", function (e) {
        e.stopPropagation(); // 부모 클릭 방지
        const $btn = $(this);
        const deliveryId = $btn.data("read");
        const link = $btn.data("link");
        const $item = $btn.closest(".notif-item");
        const label = ($btn.text() || "").trim();

        if (label === "읽음") {
            // 읽음 PATCH만 하고, UI만 업데이트
            $.ajax({
                url: `/api/notification/${deliveryId}/read`,
                type: "PATCH",
                headers: {[header]: token},
                success: function () {

                    // 링크 유무에 따라 라벨 교체
                    const newLink = $btn.data("link");
                    $btn.text(newLink ? "열기" : "확인");
                    // 스타일 전환: unread -> read
                    $item.removeClass("unread").addClass("read");
                    // 빨간 점 갱신
                    loadUnreadCount();
                },
                error: function (xhr, status, error) {
                    console.error("PATCH 실패:", status, error);
                }
            });
        } else {
            // "열기"면 이동만
            if (link) window.location.href = link;
        }
    });

    // 전체 읽음 처리
    markAllBtn.on("click", function () {
        $.ajax({
            url: "/api/notification/readAll",
            type: "PATCH",
            headers: {[header]: token},
            success: function () {
                loadNotifications();
                loadUnreadCount();
            }
        });
    });

    // 초기 실행
    loadUnreadCount();
    setInterval(loadUnreadCount, 10000); // 10초마다 갱신
});