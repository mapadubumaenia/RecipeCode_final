package com.RecipeCode.teamproject.common;

import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.recipeTag.entity.RecipeTag;

import com.RecipeCode.teamproject.reci.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;

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

    //  TODO: RecipeTag
    @Mapping(source = "recipes.uuid", target = "uuid")
    RecipeTagDto toDto(RecipeTag recipeTag);
    @Mapping(source = "uuid", target = "recipes.uuid")
    RecipeTag toEntity(RecipeTagDto recipeTagDto);


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
    RecipeContentDto toDto(RecipeContent recipeContent);
    @Mapping(target = "recipes", ignore = true)                  // uuid -> Recipes 변환은 서비스에서 처리
    @Mapping(target = "recipeImage", ignore = true)
    RecipeContent toEntity(RecipeContentDto recipeContentDto);


//  Ingredient <-> IngredientDto
    @Mapping(source = "recipes.uuid", target = "recipesUuid")
    IngredientDto toDto(Ingredient ingredient);

    @Mapping(target = "recipes", ignore = true)                 // UUID -> Recipes 객체 변환은 서비스에서 직접 처리
    Ingredient toEntity(IngredientDto ingredientDto);
}
