package com.RecipeCode.teamproject.reci.function.emailCertify.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="EMAIL_CERTIFY")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(
        name = "SQ_EMAIL_CERTIFY_KEY_JPA",
        sequenceName = "EMAIL_CERTIFY_KEY",
        allocationSize = 1
)
@Builder
public class EmailCertify{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_EMAIL_CERTIFY_KEY_JPA")
    private Long tokenId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL", nullable = false)
    private Member member;
    private String code;
    private Date expiryAt;
    private String used;
    private int attempts;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", updatable = false)
    private Date createdAt;

//   insert 직전에 자동으로 값 넣기
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
}
