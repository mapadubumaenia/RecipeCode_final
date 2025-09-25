$(function () {
    const notifBtn   = $("#btnNotif");
    const notifPanel = $("#notifPanel");
    const notifList  = $("#notifList");
    const notifDot   = $(".notif-dot");
    const markAllBtn = $("#markAll");
    const closeBtn   = $("#closeNotif");

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
                const el = $(`
                    <div class="notif-item ${item.read ? '' : 'unread'}" data-id="${item.deliveryId}">
                        <div class="ic">${iconOf(item.notification.event)}</div>
                        <div>
                            <div class="msg">${item.notification.message}</div>
                            <div class="time">${timeAgo(item.notification.insertTime)}</div>
                        </div>
                        <div>
                            <button class="btn small ghost" data-read="${item.deliveryId}">
                                ${item.read ? "열기" : "읽음"}
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

    // 이벤트 → 아이콘 매핑
    function iconOf(type) {
        switch (type) {
            case "COMMENT": return "💬";
            case "LIKE": return "❤️";
            case "FOLLOW": return "👥";
            default: return "🔔";
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

    // 개별 알림 읽음 처리
    notifList.on("click", "button[data-read]", function () {
        const deliveryId = $(this).data("read");
        $.ajax({
            url: `/api/notification/${deliveryId}/read`,
            type: "PATCH",
            success: function () {
                loadNotifications();
                loadUnreadCount();
            }
        });
    });

    // 전체 읽음 처리
    markAllBtn.on("click", function () {
        $.ajax({
            url: "/api/notification/readAll",
            type: "PATCH",
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
