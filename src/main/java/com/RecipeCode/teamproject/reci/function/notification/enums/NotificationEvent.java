package com.RecipeCode.teamproject.reci.function.notification.enums;

public enum NotificationEvent {
    FOLLOW("님이 회원님을 팔로우했습니다."),
    COMMENT("님이 회원님의 레시피에 댓글을 달았습니다."),
    LIKE("님이 회원님의 레시피를 좋아합니다.");

    private final String template;

    NotificationEvent(String template) {
        this.template = template;
    }

    public String formatMessage(String actorName) {
        return actorName + template;
    }

    public String getCode() {
        return this.name(); // "FOLLOW", "COMMENT", "LIKE"
    }
}
