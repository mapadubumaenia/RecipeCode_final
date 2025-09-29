package com.RecipeCode.teamproject.common;


import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.dto.RecipesLikesDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.entity.RecipesLikes;
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
public interface RecipeMapStruct {

    /*
    * Ingredient
    * */

    // DTO → Entity
    @Mapping(source = "recipesUuid", target = "recipes.uuid")
    Ingredient toIngredientEntity(IngredientDto dto);

    // Entity → DTO
    @Mapping(source = "recipes.uuid", target = "recipesUuid")

    IngredientDto toIngredientDto(Ingredient entity);

    // 더티체킹
    void updateIngredient(IngredientDto ingredientDto, @MappingTarget Ingredient ingredient);

    // 리스트 변환
    List<IngredientDto> toIngredientDtoList(List<Ingredient> entities);
    List<Ingredient> toIngredientEntityList(List<IngredientDto> dtos);

    /*
    *  RecipesLikes
    * */
    // DTO -> Entity
    @Mapping(source = "uuid", target = "recipes.uuid")
    RecipesLikes toRecipesLikesEntity(RecipesLikesDto dto);
    @Mapping(source = "recipes.uuid", target = "uuid")
    RecipesLikesDto toRecipesLikesDto(RecipesLikes entity);

    /*
     * Tag
     * */

    // DTO → Entity
    Tag toTagEntity(TagDto dto);

    // Entity → DTO
    TagDto toTagDto(Tag entity);

    // 리스트 변환
    List<TagDto> toTagDtoList(List<Tag> entities);
    List<Tag> toTagEntityList(List<TagDto> dtos);

    /*
    * RecipeTag
    * */

    @Mapping(source = "recipes.uuid", target = "recipeUuid")
    @Mapping(source = "tag.tagId", target = "tagId")
    @Mapping(source = "tag.tag", target = "tagName")
    RecipeTagDto toRecipeTagDto(RecipeTag entity);

    @Mapping(source = "recipeUuid", target = "recipes.uuid")
    @Mapping(source = "tagId", target = "tag.tagId")
    RecipeTag toRecipeTagEntity(RecipeTagDto dto);

    List<RecipeTagDto> toRecipeTagDtoList(List<RecipeTag> entities);
    List<RecipeTag> toRecipeTagEntityList(List<RecipeTagDto> dtos);

    // Dirty Checking (태그명은 Tag 엔티티에서만 변경되므로 여기선 주로 연결관계 업데이트)
    @Mapping(target = "recipes", ignore = true)
//    @Mapping(target = "tag", ignore = true)
    void updateRecipeTag(RecipeTagDto dto, @MappingTarget RecipeTag entity);


    // RecipeTag → TagDto 변환
    default List<TagDto> mapRecipeTagsToTagDtos(List<RecipeTag> recipeTags) {
        if (recipeTags == null) return null;

        return recipeTags.stream()
                .map(rt -> new TagDto(rt.getTag().getTagId(), rt.getTag().getTag()))
                .toList();
    }

    /* ==========================
       Recipes
       ========================== */
    @Mapping(source = "member.userEmail", target = "userEmail")
    @Mapping(source = "member.userId", target = "userId")
    @Mapping(source = "recipeTag", target = "tags")  // RecipeTag → TagDto 변환 필요
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "nickname",        source = "member.nickname")
    @Mapping(target = "userLocation",    source = "member.userLocation")
    @Mapping(target = "profileImageUrl", source = "member.profileImageUrl")
    RecipesDto toRecipeDto(Recipes recipes);

    List<RecipesDto> toRecipeDtoList(List<Recipes> recipes);

    @Mapping(source = "userEmail", target = "member.userEmail")
    @Mapping(source = "userId", target = "member.userId")
    @Mapping(target = "thumbnail", ignore = true)   // 썸네일은 Service 단에서 처리
    @Mapping(target = "recipeTag", ignore = true)   // 태그 연결은 RecipeTagService에서 처리
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "reportCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    Recipes toRecipeEntity(RecipesDto dto);

    List<Recipes> toRecipeEntityList(List<RecipesDto> recipesDtos);

    // Dirty Checking
    @Mapping(target = "uuid", ignore = true)  // PK는 수정 X
    @Mapping(target = "member", ignore = true) // member는 바꾸지 않음
    @Mapping(target = "recipeTag", ignore = true) // 태그는 별도 관리
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "reportCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
//    @Mapping(target = "liked", ignore = true)
    void updateRecipe(RecipesDto dto, @MappingTarget Recipes entity);

    /* ==========================
        RecipeContent
       ========================== */

    // Entity → DTO
    @Mapping(source = "recipes.uuid", target = "recipes")
    @Mapping(target = "recipeImage", ignore = true)
    RecipeContentDto toRecipeContentDto(RecipeContent entity);

    List<RecipeContentDto> toRecipeContentDtoList(List<RecipeContent> entities);

    // DTO → Entity
    @Mapping(source = "recipes", target = "recipes.uuid")
    @Mapping(target = "recipeImage", ignore = true) // 이미지 byte[]는 서비스에서 처리
    @Mapping(target = "recipeImageUrl", ignore = true)    // URL은 서비스에서
    RecipeContent toRecipeContentEntity(RecipeContentDto dto);

    List<RecipeContent> toRecipeContentEntityList(List<RecipeContentDto> dtos);

    // Dirty Checking용 업데이트
    @Mapping(target = "recipes", ignore = true)
    @Mapping(target = "recipeImage", ignore = true)
    @Mapping(target = "recipeImageUrl", ignore = true)
    void updateRecipeContent(RecipeContentDto dto, @MappingTarget RecipeContent entity);
}
