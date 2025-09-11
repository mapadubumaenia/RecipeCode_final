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
    RecipeTagDto toDto(RecipeTag recipeTag);

    RecipeTag toEntity(RecipeTagDto recipeTagDto);

// Recipes <-> RecipesDto
    RecipesDto toDto(Recipes recipes);
    Recipes toEntity(RecipesDto recipesDto);

// Comments <-> CommentsDto
    @Mapping(source = "userEmail.userId", target = "userId")
    @Mapping(source = "userEmail.userEmail", target = "userEmail")
    @Mapping(source = "uuid.uuid", target = "recipeUuid")
    @Mapping(source = "parentId.commentsId", target = "parentId")
    CommentsDto toDto(Comments comments);
    @Mapping(source = "userEmail", target = "userEmail.userEmail")
    @Mapping(source = "recipeUuid", target = "uuid.uuid")
    @Mapping(source = "parentId", target = "parentId.commentsId")
    Comments toEntity(CommentsDto commentsDto);


//  RecipeContent <-> RecipeContentDto
    @Mapping(source = "uuid.uuid", target = "recipeUuid")
    RecipeContentDto toDto(RecipeContent recipeContent);
    @Mapping(source = "recipeUuid", target = "uuid.uuid")
    @Mapping(target = "recipes", ignore = true)
    RecipeContent toEntity(RecipeContentDto recipeContentDto);

//  Ingredient <-> IngredientDto
    @Mapping(source = "recipeUuid.uuid", target = "recipeUuid")
    IngredientDto toDto(Ingredient ingredient);

    @Mapping(source = "recipeUuid", target = "recipeUuid.uuid")
    @Mapping(target = "recipeUuid", ignore = true)
    Ingredient toEntity(IngredientDto ingredientDto);
}
