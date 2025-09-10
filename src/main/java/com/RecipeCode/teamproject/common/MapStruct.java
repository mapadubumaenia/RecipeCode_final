package com.RecipeCode.teamproject.common;


import com.RecipeCode.teamproject.reci.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.comments.entity.Comments;
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

// Recipes <-> RecipesDto
    RecipesDto toDto(Recipes recipes);
    Recipes toEntity(RecipesDto recipesDto);

// Comments <-> CommentsDto
    @Mapping(source = "userEmail.userId", target = "userId")
    @Mapping(source = "userEmail.userEmail", target = "userEmail")
    @Mapping(source = "uuid.uuid", target = "recipeUuid")
    @Mapping(source = "parentId.commentsId", target = "parentId")
    Comments toDto(CommentsDto commentsDto);
    @Mapping(source = "userEmail", target = "userEmail.userEmail")
    @Mapping(source = "recipeUuid", target = "uuid.uuid")
    @Mapping(source = "parentId", target = "parentId.commentsId")
    CommentsDto toEntity(Comments comments);

}
