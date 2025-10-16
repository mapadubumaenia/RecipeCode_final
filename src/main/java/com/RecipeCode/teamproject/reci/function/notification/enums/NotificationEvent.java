package com.RecipeCode.teamproject.reci.function.notification.enums;

public enum NotificationEvent {
    FOLLOW("%s님이 회원님을 팔로우했습니다."),
    COMMENT("%s님이 회원님의 레시피에 댓글을 달았습니다."),
    LIKE("%s님이 회원님의 레시피를 좋아합니다."),

    // 신고 결과 알림
    RECIPE_REPORT_RESULT("신고하신 '%s' 게시글이 '%s'로 처리되었습니다.");

    private final String template;

    NotificationEvent(String template) {
        this.template = template;
    }

    // 상황별 가변 인자
    public String formatMessage(Object... args) {
        return String.format(template, args);
    }

   // 기존 코드 호환용 (FOLLOW/COMMENT/LIKE 등 1개 인자)
    @Deprecated
    public String formatMessage(String actorName) {
        return String.format(template, actorName);
    }

    public String getCode() { return name(); }
}
