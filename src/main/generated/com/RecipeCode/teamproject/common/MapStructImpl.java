package com.RecipeCode.teamproject.common;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import java.util.Arrays;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-16T11:35:39+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Azul Systems, Inc.)"
)
@Component
public class MapStructImpl implements MapStruct {

    @Override
    public FaqDto toDto(Faq faq) {
        if ( faq == null ) {
            return null;
        }

        FaqDto faqDto = new FaqDto();

        faqDto.setFaq_num( faq.getFaq_num() );
        faqDto.setFaq_question( faq.getFaq_question() );
        faqDto.setFaq_answer( faq.getFaq_answer() );
        faqDto.setFaq_tag( faq.getFaq_tag() );

        return faqDto;
    }

    @Override
    public Faq toEntity(FaqDto faqDto) {
        if ( faqDto == null ) {
            return null;
        }

        Faq faq = new Faq();

        faq.setFaq_num( faqDto.getFaq_num() );
        faq.setFaq_question( faqDto.getFaq_question() );
        faq.setFaq_answer( faqDto.getFaq_answer() );
        faq.setFaq_tag( faqDto.getFaq_tag() );

        return faq;
    }

    @Override
    public void updateFromDto(FaqDto faqDto, Faq faq) {
        if ( faqDto == null ) {
            return;
        }

        if ( faqDto.getFaq_num() != null ) {
            faq.setFaq_num( faqDto.getFaq_num() );
        }
        if ( faqDto.getFaq_question() != null ) {
            faq.setFaq_question( faqDto.getFaq_question() );
        }
        if ( faqDto.getFaq_answer() != null ) {
            faq.setFaq_answer( faqDto.getFaq_answer() );
        }
        if ( faqDto.getFaq_tag() != null ) {
            faq.setFaq_tag( faqDto.getFaq_tag() );
        }
    }

    @Override
    public CommentsDto toDto(Comments comments) {
        if ( comments == null ) {
            return null;
        }

        CommentsDto commentsDto = new CommentsDto();

        commentsDto.setRecipeUuid( commentsRecipesUuid( comments ) );
        commentsDto.setUserEmail( commentsMemberUserEmail( comments ) );
        commentsDto.setUserId( commentsMemberUserId( comments ) );
        commentsDto.setParentId( commentsParentIdCommentsId( comments ) );
        commentsDto.setCommentsId( comments.getCommentsId() );
        commentsDto.setCommentsContent( comments.getCommentsContent() );
        commentsDto.setLikeCount( comments.getLikeCount() );
        commentsDto.setReportCount( comments.getReportCount() );
        commentsDto.setInsertTime( comments.getInsertTime() );
        commentsDto.setUpdateTime( comments.getUpdateTime() );

        return commentsDto;
    }

    @Override
    public Comments toEntity(CommentsDto commentsDto) {
        if ( commentsDto == null ) {
            return null;
        }

        Comments.CommentsBuilder comments = Comments.builder();

        comments.commentsId( commentsDto.getCommentsId() );
        comments.commentsContent( commentsDto.getCommentsContent() );
        comments.likeCount( commentsDto.getLikeCount() );
        comments.reportCount( commentsDto.getReportCount() );

        return comments.build();
    }

    @Override
    public MemberDto toDto(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberDto memberDto = new MemberDto();

        memberDto.setUserEmail( member.getUserEmail() );
        memberDto.setUserId( member.getUserId() );
        memberDto.setNickname( member.getNickname() );
        memberDto.setPassword( member.getPassword() );
        memberDto.setUserLocation( member.getUserLocation() );
        memberDto.setUserIntroduce( member.getUserIntroduce() );
        memberDto.setUserWebsite( member.getUserWebsite() );
        memberDto.setUserInsta( member.getUserInsta() );
        memberDto.setUserYoutube( member.getUserYoutube() );
        memberDto.setUserBlog( member.getUserBlog() );
        memberDto.setUserInterestTag( member.getUserInterestTag() );
        memberDto.setProfileStatus( member.getProfileStatus() );
        memberDto.setRole( member.getRole() );
        memberDto.setProfileImageUrl( member.getProfileImageUrl() );
        byte[] profileImage = member.getProfileImage();
        if ( profileImage != null ) {
            memberDto.setProfileImage( Arrays.copyOf( profileImage, profileImage.length ) );
        }

        return memberDto;
    }

    @Override
    public Member toEntity(MemberDto memberDto) {
        if ( memberDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.userEmail( memberDto.getUserEmail() );
        member.userId( memberDto.getUserId() );
        member.nickname( memberDto.getNickname() );
        member.password( memberDto.getPassword() );
        member.userLocation( memberDto.getUserLocation() );
        member.userIntroduce( memberDto.getUserIntroduce() );
        member.userWebsite( memberDto.getUserWebsite() );
        member.userInsta( memberDto.getUserInsta() );
        member.userYoutube( memberDto.getUserYoutube() );
        member.userBlog( memberDto.getUserBlog() );
        member.userInterestTag( memberDto.getUserInterestTag() );
        member.profileStatus( memberDto.getProfileStatus() );
        member.role( memberDto.getRole() );
        byte[] profileImage = memberDto.getProfileImage();
        if ( profileImage != null ) {
            member.profileImage( Arrays.copyOf( profileImage, profileImage.length ) );
        }
        member.profileImageUrl( memberDto.getProfileImageUrl() );

        return member.build();
    }

    private String commentsRecipesUuid(Comments comments) {
        if ( comments == null ) {
            return null;
        }
        Recipes recipes = comments.getRecipes();
        if ( recipes == null ) {
            return null;
        }
        String uuid = recipes.getUuid();
        if ( uuid == null ) {
            return null;
        }
        return uuid;
    }

    private String commentsMemberUserEmail(Comments comments) {
        if ( comments == null ) {
            return null;
        }
        Member member = comments.getMember();
        if ( member == null ) {
            return null;
        }
        String userEmail = member.getUserEmail();
        if ( userEmail == null ) {
            return null;
        }
        return userEmail;
    }

    private String commentsMemberUserId(Comments comments) {
        if ( comments == null ) {
            return null;
        }
        Member member = comments.getMember();
        if ( member == null ) {
            return null;
        }
        String userId = member.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private Long commentsParentIdCommentsId(Comments comments) {
        if ( comments == null ) {
            return null;
        }
        Comments parentId = comments.getParentId();
        if ( parentId == null ) {
            return null;
        }
        Long commentsId = parentId.getCommentsId();
        if ( commentsId == null ) {
            return null;
        }
        return commentsId;
    }
}
