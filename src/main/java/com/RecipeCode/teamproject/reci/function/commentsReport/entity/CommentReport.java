package com.RecipeCode.teamproject.reci.function.commentsReport.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "COMMENT_REPORT")
@SequenceGenerator(
        name = "SQ_COMMENT_REPORT_JPA",
        sequenceName = "COMMENT_REPORT_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "reportId", callSuper = false)
public class CommentReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_COMMENT_REPORT_JPA")
    private Long reportId;

    private String adminEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL")
    private Member member; // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMENTS_ID")
    private Comments comments; // 신고대상댓글

    private String reason;
    private Long reportStatus = 0L;
    private Long reportType;
}
