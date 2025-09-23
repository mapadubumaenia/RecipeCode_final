package com.RecipeCode.teamproject.reci.auth.membertag.dto;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberTagDto {
    private Long memberTagId;
    private String tagUserEmail;
    private Long tagId;
    private String tagName;

    //    등록용
    public  MemberTagDto(String tagUserEmail,
                         Long tagId) {
        this.tagUserEmail = tagUserEmail;
        this.tagId = tagId;
    }
}
