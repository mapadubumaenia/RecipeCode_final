$(function () {

    // 안 읽은 알림 개수 불러오기
    function loadUnreadCount() {
        $.get("/api/notification/unreadCount", function (count) {
            $("#noti-count").text(count);
        });
    }

    // 알림 목록 불러오기
    function loadNotifications() {
        $.get("/api/notification", function (list) {
            const $list = $("#noti-list ul");
            $list.empty();

            list.forEach(item => {
                const li = `
                    <li data-id="${item.deliveryId}" class="${item.isRead ? 'read' : 'unread'}">
                        ${item.notification.message}
                    </li>`;
                $list.append(li);
            });
        });
    }

    // 알림 버튼 클릭 → 드롭다운 토글
    $("#btnNotif").on("click", function () {
        $("#noti-list").toggleClass("hidden");
        loadNotifications(); // 버튼 눌렀을 때 최신화
    });

    // 알림 클릭 → 읽음 처리
    $(document).on("click", "#noti-list li", function () {
        const deliveryId = $(this).data("id");
        $.ajax({
            url: `/api/notification/${deliveryId}/read`,
            type: "PATCH",
            success: function () {
                loadUnreadCount();
                loadNotifications();
            }
        });
    });

    // 초기 실행
    loadUnreadCount();
    setInterval(loadUnreadCount, 10000); // 10초마다 갱신
});
