package com.RecipeCode.teamproject.common;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-16T11:35:39+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Azul Systems, Inc.)"
)
@Component
public class RecipeMapStructImpl implements RecipeMapStruct {

    @Override
    public Ingredient toIngredientEntity(IngredientDto dto) {
        if ( dto == null ) {
            return null;
        }

        Ingredient.IngredientBuilder ingredient = Ingredient.builder();

        ingredient.recipes( ingredientDtoToRecipes( dto ) );
        ingredient.id( dto.getId() );
        ingredient.ingredientName( dto.getIngredientName() );
        ingredient.ingredientAmount( dto.getIngredientAmount() );
        ingredient.sortOrder( dto.getSortOrder() );

        return ingredient.build();
    }

    @Override
    public IngredientDto toIngredientDto(Ingredient entity) {
        if ( entity == null ) {
            return null;
        }

        IngredientDto ingredientDto = new IngredientDto();

        ingredientDto.setRecipesUuid( entityRecipesUuid( entity ) );
        ingredientDto.setId( entity.getId() );
        ingredientDto.setIngredientName( entity.getIngredientName() );
        ingredientDto.setIngredientAmount( entity.getIngredientAmount() );
        ingredientDto.setSortOrder( entity.getSortOrder() );

        return ingredientDto;
    }

    @Override
    public void updateIngredient(IngredientDto ingredientDto, Ingredient ingredient) {
        if ( ingredientDto == null ) {
            return;
        }

        if ( ingredientDto.getId() != null ) {
            ingredient.setId( ingredientDto.getId() );
        }
        if ( ingredientDto.getIngredientName() != null ) {
            ingredient.setIngredientName( ingredientDto.getIngredientName() );
        }
        if ( ingredientDto.getIngredientAmount() != null ) {
            ingredient.setIngredientAmount( ingredientDto.getIngredientAmount() );
        }
        if ( ingredientDto.getSortOrder() != null ) {
            ingredient.setSortOrder( ingredientDto.getSortOrder() );
        }
    }

    @Override
    public List<IngredientDto> toIngredientDtoList(List<Ingredient> entities) {
        if ( entities == null ) {
            return null;
        }

        List<IngredientDto> list = new ArrayList<IngredientDto>( entities.size() );
        for ( Ingredient ingredient : entities ) {
            list.add( toIngredientDto( ingredient ) );
        }

        return list;
    }

    @Override
    public List<Ingredient> toIngredientEntityList(List<IngredientDto> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Ingredient> list = new ArrayList<Ingredient>( dtos.size() );
        for ( IngredientDto ingredientDto : dtos ) {
            list.add( toIngredientEntity( ingredientDto ) );
        }

        return list;
    }

    @Override
    public Tag toTagEntity(TagDto dto) {
        if ( dto == null ) {
            return null;
        }

        Tag tag = new Tag();

        tag.setTagId( dto.getTagId() );
        tag.setTag( dto.getTag() );

        return tag;
    }

    @Override
    public TagDto toTagDto(Tag entity) {
        if ( entity == null ) {
            return null;
        }

        TagDto tagDto = new TagDto();

        tagDto.setTagId( entity.getTagId() );
        tagDto.setTag( entity.getTag() );

        return tagDto;
    }

    @Override
    public List<TagDto> toTagDtoList(List<Tag> entities) {
        if ( entities == null ) {
            return null;
        }

        List<TagDto> list = new ArrayList<TagDto>( entities.size() );
        for ( Tag tag : entities ) {
            list.add( toTagDto( tag ) );
        }

        return list;
    }

    @Override
    public List<Tag> toTagEntityList(List<TagDto> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Tag> list = new ArrayList<Tag>( dtos.size() );
        for ( TagDto tagDto : dtos ) {
            list.add( toTagEntity( tagDto ) );
        }

        return list;
    }

    @Override
    public RecipeTagDto toRecipeTagDto(RecipeTag entity) {
        if ( entity == null ) {
            return null;
        }

        RecipeTagDto recipeTagDto = new RecipeTagDto();

        recipeTagDto.setRecipeUuid( entityRecipesUuid1( entity ) );
        recipeTagDto.setTagId( entityTagTagId( entity ) );
        recipeTagDto.setTagName( entityTagTag( entity ) );
        recipeTagDto.setRecipeTagId( entity.getRecipeTagId() );

        return recipeTagDto;
    }

    @Override
    public RecipeTag toRecipeTagEntity(RecipeTagDto dto) {
        if ( dto == null ) {
            return null;
        }

        RecipeTag recipeTag = new RecipeTag();

        recipeTag.setRecipes( recipeTagDtoToRecipes( dto ) );
        recipeTag.setTag( recipeTagDtoToTag( dto ) );
        recipeTag.setRecipeTagId( dto.getRecipeTagId() );

        return recipeTag;
    }

    @Override
    public List<RecipeTagDto> toRecipeTagDtoList(List<RecipeTag> entities) {
        if ( entities == null ) {
            return null;
        }

        List<RecipeTagDto> list = new ArrayList<RecipeTagDto>( entities.size() );
        for ( RecipeTag recipeTag : entities ) {
            list.add( toRecipeTagDto( recipeTag ) );
        }

        return list;
    }

    @Override
    public List<RecipeTag> toRecipeTagEntityList(List<RecipeTagDto> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<RecipeTag> list = new ArrayList<RecipeTag>( dtos.size() );
        for ( RecipeTagDto recipeTagDto : dtos ) {
            list.add( toRecipeTagEntity( recipeTagDto ) );
        }

        return list;
    }

    @Override
    public void updateRecipeTag(RecipeTagDto dto, RecipeTag entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getRecipeTagId() != null ) {
            entity.setRecipeTagId( dto.getRecipeTagId() );
        }
    }

    @Override
    public RecipesDto toRecipeDto(Recipes recipes) {
        if ( recipes == null ) {
            return null;
        }

        RecipesDto recipesDto = new RecipesDto();

        recipesDto.setUserEmail( recipesMemberUserEmail( recipes ) );
        recipesDto.setUserId( recipesMemberUserId( recipes ) );
        recipesDto.setTags( mapRecipeTagsToTagDtos( recipes.getRecipeTag() ) );
        recipesDto.setUuid( recipes.getUuid() );
        recipesDto.setRecipeTitle( recipes.getRecipeTitle() );
        recipesDto.setRecipeIntro( recipes.getRecipeIntro() );
        recipesDto.setRecipeCategory( recipes.getRecipeCategory() );
        recipesDto.setPostStatus( recipes.getPostStatus() );
        recipesDto.setDifficulty( recipes.getDifficulty() );
        recipesDto.setCookingTime( recipes.getCookingTime() );
        recipesDto.setThumbnailUrl( recipes.getThumbnailUrl() );
        recipesDto.setRecipeType( recipes.getRecipeType() );
        recipesDto.setVideoUrl( recipes.getVideoUrl() );
        recipesDto.setVideoText( recipes.getVideoText() );
        recipesDto.setViewCount( recipes.getViewCount() );
        recipesDto.setLikeCount( recipes.getLikeCount() );
        recipesDto.setReportCount( recipes.getReportCount() );
        recipesDto.setCommentCount( recipes.getCommentCount() );
        recipesDto.setInsertTime( recipes.getInsertTime() );
        recipesDto.setUpdateTime( recipes.getUpdateTime() );

        return recipesDto;
    }

    @Override
    public List<RecipesDto> toRecipeDtoList(List<Recipes> recipes) {
        if ( recipes == null ) {
            return null;
        }

        List<RecipesDto> list = new ArrayList<RecipesDto>( recipes.size() );
        for ( Recipes recipes1 : recipes ) {
            list.add( toRecipeDto( recipes1 ) );
        }

        return list;
    }

    @Override
    public Recipes toRecipeEntity(RecipesDto dto) {
        if ( dto == null ) {
            return null;
        }

        Recipes recipes = new Recipes();

        recipes.setMember( recipesDtoToMember( dto ) );
        recipes.setUuid( dto.getUuid() );
        recipes.setRecipeTitle( dto.getRecipeTitle() );
        recipes.setRecipeIntro( dto.getRecipeIntro() );
        recipes.setRecipeCategory( dto.getRecipeCategory() );
        recipes.setCookingTime( dto.getCookingTime() );
        recipes.setThumbnailUrl( dto.getThumbnailUrl() );
        recipes.setRecipeType( dto.getRecipeType() );
        recipes.setVideoUrl( dto.getVideoUrl() );
        recipes.setVideoText( dto.getVideoText() );
        recipes.setPostStatus( dto.getPostStatus() );
        recipes.setDifficulty( dto.getDifficulty() );

        return recipes;
    }

    @Override
    public List<Recipes> toRecipeEntityList(List<RecipesDto> recipesDtos) {
        if ( recipesDtos == null ) {
            return null;
        }

        List<Recipes> list = new ArrayList<Recipes>( recipesDtos.size() );
        for ( RecipesDto recipesDto : recipesDtos ) {
            list.add( toRecipeEntity( recipesDto ) );
        }

        return list;
    }

    @Override
    public void updateRecipe(RecipesDto dto, Recipes entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getRecipeTitle() != null ) {
            entity.setRecipeTitle( dto.getRecipeTitle() );
        }
        if ( dto.getRecipeIntro() != null ) {
            entity.setRecipeIntro( dto.getRecipeIntro() );
        }
        if ( dto.getRecipeCategory() != null ) {
            entity.setRecipeCategory( dto.getRecipeCategory() );
        }
        if ( dto.getCookingTime() != null ) {
            entity.setCookingTime( dto.getCookingTime() );
        }
        if ( dto.getThumbnailUrl() != null ) {
            entity.setThumbnailUrl( dto.getThumbnailUrl() );
        }
        if ( dto.getRecipeType() != null ) {
            entity.setRecipeType( dto.getRecipeType() );
        }
        if ( dto.getVideoUrl() != null ) {
            entity.setVideoUrl( dto.getVideoUrl() );
        }
        if ( dto.getVideoText() != null ) {
            entity.setVideoText( dto.getVideoText() );
        }
        if ( dto.getPostStatus() != null ) {
            entity.setPostStatus( dto.getPostStatus() );
        }
        if ( dto.getDifficulty() != null ) {
            entity.setDifficulty( dto.getDifficulty() );
        }
    }

    @Override
    public RecipeContentDto toRecipeContentDto(RecipeContent entity) {
        if ( entity == null ) {
            return null;
        }

        RecipeContentDto recipeContentDto = new RecipeContentDto();

        recipeContentDto.setRecipes( entityRecipesUuid2( entity ) );
        recipeContentDto.setStepId( entity.getStepId() );
        recipeContentDto.setRecipeImageUrl( entity.getRecipeImageUrl() );
        recipeContentDto.setStepExplain( entity.getStepExplain() );
        recipeContentDto.setStepOrder( entity.getStepOrder() );

        return recipeContentDto;
    }

    @Override
    public List<RecipeContentDto> toRecipeContentDtoList(List<RecipeContent> entities) {
        if ( entities == null ) {
            return null;
        }

        List<RecipeContentDto> list = new ArrayList<RecipeContentDto>( entities.size() );
        for ( RecipeContent recipeContent : entities ) {
            list.add( toRecipeContentDto( recipeContent ) );
        }

        return list;
    }

    @Override
    public RecipeContent toRecipeContentEntity(RecipeContentDto dto) {
        if ( dto == null ) {
            return null;
        }

        RecipeContent recipeContent = new RecipeContent();

        recipeContent.setRecipes( recipeContentDtoToRecipes( dto ) );
        recipeContent.setStepId( dto.getStepId() );
        recipeContent.setRecipeImageUrl( dto.getRecipeImageUrl() );
        recipeContent.setStepExplain( dto.getStepExplain() );
        recipeContent.setStepOrder( dto.getStepOrder() );

        return recipeContent;
    }

    @Override
    public List<RecipeContent> toRecipeContentEntityList(List<RecipeContentDto> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<RecipeContent> list = new ArrayList<RecipeContent>( dtos.size() );
        for ( RecipeContentDto recipeContentDto : dtos ) {
            list.add( toRecipeContentEntity( recipeContentDto ) );
        }

        return list;
    }

    protected Recipes ingredientDtoToRecipes(IngredientDto ingredientDto) {
        if ( ingredientDto == null ) {
            return null;
        }

        Recipes recipes = new Recipes();

        recipes.setUuid( ingredientDto.getRecipesUuid() );

        return recipes;
    }

    private String entityRecipesUuid(Ingredient ingredient) {
        if ( ingredient == null ) {
            return null;
        }
        Recipes recipes = ingredient.getRecipes();
        if ( recipes == null ) {
            return null;
        }
        String uuid = recipes.getUuid();
        if ( uuid == null ) {
            return null;
        }
        return uuid;
    }

    private String entityRecipesUuid1(RecipeTag recipeTag) {
        if ( recipeTag == null ) {
            return null;
        }
        Recipes recipes = recipeTag.getRecipes();
        if ( recipes == null ) {
            return null;
        }
        String uuid = recipes.getUuid();
        if ( uuid == null ) {
            return null;
        }
        return uuid;
    }

    private Long entityTagTagId(RecipeTag recipeTag) {
        if ( recipeTag == null ) {
            return null;
        }
        Tag tag = recipeTag.getTag();
        if ( tag == null ) {
            return null;
        }
        Long tagId = tag.getTagId();
        if ( tagId == null ) {
            return null;
        }
        return tagId;
    }

    private String entityTagTag(RecipeTag recipeTag) {
        if ( recipeTag == null ) {
            return null;
        }
        Tag tag = recipeTag.getTag();
        if ( tag == null ) {
            return null;
        }
        String tag1 = tag.getTag();
        if ( tag1 == null ) {
            return null;
        }
        return tag1;
    }

    protected Recipes recipeTagDtoToRecipes(RecipeTagDto recipeTagDto) {
        if ( recipeTagDto == null ) {
            return null;
        }

        Recipes recipes = new Recipes();

        recipes.setUuid( recipeTagDto.getRecipeUuid() );

        return recipes;
    }

    protected Tag recipeTagDtoToTag(RecipeTagDto recipeTagDto) {
        if ( recipeTagDto == null ) {
            return null;
        }

        Tag tag = new Tag();

        tag.setTagId( recipeTagDto.getTagId() );

        return tag;
    }

    private String recipesMemberUserEmail(Recipes recipes) {
        if ( recipes == null ) {
            return null;
        }
        Member member = recipes.getMember();
        if ( member == null ) {
            return null;
        }
        String userEmail = member.getUserEmail();
        if ( userEmail == null ) {
            return null;
        }
        return userEmail;
    }

    private String recipesMemberUserId(Recipes recipes) {
        if ( recipes == null ) {
            return null;
        }
        Member member = recipes.getMember();
        if ( member == null ) {
            return null;
        }
        String userId = member.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    protected Member recipesDtoToMember(RecipesDto recipesDto) {
        if ( recipesDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.userEmail( recipesDto.getUserEmail() );
        member.userId( recipesDto.getUserId() );

        return member.build();
    }

    private String entityRecipesUuid2(RecipeContent recipeContent) {
        if ( recipeContent == null ) {
            return null;
        }
        Recipes recipes = recipeContent.getRecipes();
        if ( recipes == null ) {
            return null;
        }
        String uuid = recipes.getUuid();
        if ( uuid == null ) {
            return null;
        }
        return uuid;
    }

    protected Recipes recipeContentDtoToRecipes(RecipeContentDto recipeContentDto) {
        if ( recipeContentDto == null ) {
            return null;
        }

        Recipes recipes = new Recipes();

        recipes.setUuid( recipeContentDto.getRecipes() );

        return recipes;
    }
}
