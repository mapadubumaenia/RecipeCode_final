package com.RecipeCode.teamproject.common;

import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;

import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;

import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // null 제외 기능(update 시 사용)
)
public interface MapStruct {
    //  TODO: Faq
    FaqDto toDto(Faq faq);
    Faq toEntity(FaqDto faqDto);
    // TODO: 수정 시 사용: dirty checking 기능(save() 없이 수정 가능)
    void updateFromDto(FaqDto faqDto, @MappingTarget Faq faq);

    //  TODO: RecipeTag <-> RecipeTagDto
    @Mapping(source = "recipes.uuid", target = "recipeUuid")
    @Mapping(source = "tag.tagId", target = "tagId")
    @Mapping(source = "tag.tag", target = "tagName")
    RecipeTagDto toDto(RecipeTag recipeTag);
    @Mapping(target = "recipes", ignore = true)
    @Mapping(target = "tag", ignore = true)
    RecipeTag toEntity(RecipeTagDto recipeTagDto);

// Tag <-> TagDto
    TagDto toDto(Tag tag);
    Tag toEntity(TagDto tagDto);

// Recipes <-> RecipesDto
    @Mapping(source = "member.userEmail", target = "userEmail")
    RecipesDto toDto(Recipes recipes);
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "member", ignore = true)
    Recipes toEntity(RecipesDto recipesDto);

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


//  RecipeContent <-> RecipeContentDto
    @Mapping(source = "recipes.uuid", target = "recipes")
//    @Mapping(source = "stepExplain", target = "stepExplain")
    RecipeContentDto toDto(RecipeContent recipeContent);
    @Mapping(target = "recipes", ignore = true)                  // uuid -> Recipes 변환은 서비스에서 처리
    @Mapping(target = "recipeImage", ignore = true)
//    @Mapping(source = "stepExplain", target = "stepExplain")
    @Mapping(source = "recipeImageUrl", target = "recipeImageUrl")
    RecipeContent toEntity(RecipeContentDto recipeContentDto);


//  Ingredient <-> IngredientDto
    @Mapping(source = "recipes.uuid", target = "recipesUuid")
    IngredientDto toDto(Ingredient ingredient);

    @Mapping(target = "recipes", ignore = true)                 // UUID -> Recipes 객체 변환은 서비스에서 직접 처리
    Ingredient toEntity(IngredientDto ingredientDto);



}
