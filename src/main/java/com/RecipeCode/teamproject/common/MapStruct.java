package com.RecipeCode.teamproject.common;


import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;

import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;

import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // null 제외 기능(update 시 사용)
)
public interface MapStruct {
    //  TODO: Faq
    FaqDto toDto(Faq faq);
    Faq toEntity(FaqDto faqDto);
    // TODO: 수정 시 사용: dirty checking 기능(save() 없이 수정 가능)
    void updateFromDto(FaqDto faqDto, @MappingTarget Faq faq);


// Comments <-> CommentsDto

    @Mapping(source = "recipes.uuid", target = "recipeUuid")
    @Mapping(source = "member.userEmail", target = "userEmail")
    @Mapping(source = "member.userId", target = "userId") // Member 엔티티에 userId 있다고 가정
    @Mapping(source = "parentId.commentsId", target = "parentId")
    CommentsDto toDto(Comments comments);
    @Mapping(target = "recipes", ignore = true)   // UUID -> Recipes 변환은 서비스에서 처리
    @Mapping(target = "member", ignore = true)    // Email -> Member 변환도 서비스에서 처리
    @Mapping(target = "parentId", ignore = true)  // parent 댓글 세팅도 서비스에서 처리
    @Mapping(target = "children", ignore = true)  // 자식 댓글은 별도 로직 필요
    Comments toEntity(CommentsDto commentsDto);




// Member <-> MemberDto
    @Mapping(target = "profileImage", ignore = true) // DTO가 MultipartFile이면 toDto시 매핑 제외
    @Mapping(source = "memberTags", target = "interestTags")
    MemberDto toDto(Member member);

    @Mapping(source = "interestTags", target = "memberTags")
    @Mapping(target = "profileImage",
            expression = "java( mapMultipartToBytes(memberDto.getProfileImage()) )")
    Member toEntity(MemberDto memberDto);
    //  변환 헬퍼
    default byte[] mapMultipartToBytes(org.springframework.web.multipart.MultipartFile file) {
        try {
            return (file == null || file.isEmpty()) ? null : file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("파일 변환 실패", e);
        }
    }

    //  추가 매핑

    // MemberTag -> TagDto
    default TagDto map(MemberTag memberTag) {
        if (memberTag == null || memberTag.getTag() == null) return null;
        Tag tag = memberTag.getTag();
        return new TagDto(tag.getTagId(), tag.getTag());
    }

    // TagDto -> MemberTag
    default MemberTag map(TagDto dto) {
        if (dto == null) return null;
        Tag tag = new Tag();
        tag.setTagId(dto.getTagId());
        tag.setTag(dto.getTag());

        MemberTag memberTag = new MemberTag();
        memberTag.setTag(tag);
        return memberTag;
    }

    //  TODO: RecipeReportDto
    @Mapping(source = "member.userEmail", target = "userEmail")
    @Mapping(target = "uuid", ignore = true)
    // recipeTitle은 무시 (삭제된 경우 안전하게 처리하기 위해)
    @Mapping(target = "recipeTitle", ignore = true)
    RecipeReportDto toDto(RecipeReport recipeReport);
    @Mapping(source = "userEmail", target = "member.userEmail")
    @Mapping(source = "uuid", target = "recipes.uuid")
    RecipeReport toEntity(RecipeReportDto recipeReportDto);


    // Follow <-> FollowDto
    @Mapping(source = "follower", target = "member")   // 팔로워 목록 조회 시
    FollowDto toFollowerDto(Follow follow);

    @Mapping(source = "following", target = "member")  // 팔로잉 목록 조회 시
    FollowDto toFollowingDto(Follow follow);

}
