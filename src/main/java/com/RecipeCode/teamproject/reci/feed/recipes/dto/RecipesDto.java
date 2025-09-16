package com.RecipeCode.teamproject.reci.feed.recipes.dto;

import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RecipesDto {

    private String uuid;            // PK
    private String userEmail;       // member FK
    private String userId;
    private String recipeTitle;
    private String recipeIntro;
    private String recipeCategory;
    private String postStatus;
    private String difficulty;
    private Long cookingTime;
    private String thumbnailUrl;
    private String recipeType;
    private String videoUrl;
    private String videoText;

    private List<RecipeContentDto> contents;
    private List<IngredientDto> ingredients;
    private List<TagDto> tags;                  // 태그 배열

    private Long viewCount;
    private Long likeCount;
    private Long reportCount;
    private Long commentCount;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

//    피드 조회용 생성자

    public RecipesDto(String uuid,
                      String recipeTitle,
                      String userEmail,
                      String recipeIntro,
                      Long likeCount,
                      Long commentCount,
                      String postStatus,
                      List<TagDto> tags,
                      LocalDateTime insertTime,
                      String recipeType,
                      String videoUrl) {
        this.uuid = uuid;
        this.recipeTitle = recipeTitle;
        this.userEmail = userEmail;
        this.recipeIntro = recipeIntro;
        this.insertTime = insertTime;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.postStatus = postStatus;
        this.tags = tags;
        this.recipeType = recipeType;
        this.videoUrl = videoUrl;
    }

//    상세페이지 조회용 생성자(필드 대부분 다 채움)
public RecipesDto(String uuid,
                  String userEmail,
                  String userId,
                  String recipeTitle,
                  String recipeIntro,
                  String recipeCategory,
                  String postStatus,
                  String difficulty,
                  Long cookingTime,
                  List<RecipeContentDto> contents,
                  List<IngredientDto> ingredients,
                  List<TagDto> tags,
                  Long viewCount,
                  Long likeCount,
                  Long commentCount,
                  String recipeType,
                  String videoUrl,
                  String videoText,
                  LocalDateTime insertTime,
                  LocalDateTime updateTime) {
    this.uuid = uuid;
    this.userEmail = userEmail;
    this.userId = userId;
    this.recipeTitle = recipeTitle;
    this.recipeIntro = recipeIntro;
    this.recipeCategory = recipeCategory;
    this.postStatus = postStatus;
    this.difficulty = difficulty;
    this.cookingTime = cookingTime;
    this.contents = contents;
    this.ingredients = ingredients;
    this.viewCount = viewCount;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.insertTime = insertTime;
    this.updateTime = updateTime;
    this.tags = tags;
    this.recipeType = recipeType;
    this.videoUrl = videoUrl;
    this.videoText = videoText;
}

//  등록 수정용 생성자
//  (자동생성 제외, 작성자가 넣는 값만)

    public RecipesDto(String recipeTitle,
                      String recipeIntro,
                      String recipeCategory,
                      String postStatus,
                      String difficulty,
                      Long cookingTime,
                      List<RecipeContentDto> contents,
                      List<IngredientDto> ingredients,
                      List<TagDto> tags,
                      String recipeType,
                      String videoUrl,
                      String videoText) {

        this.recipeTitle = recipeTitle;
        this.recipeIntro = recipeIntro;
        this.recipeCategory = recipeCategory;
        this.postStatus = postStatus;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.contents = contents;
        this.ingredients = ingredients;
        this.tags = tags;
        this.recipeType = recipeType;
        this.videoUrl = videoUrl;
        this.videoText = videoText;
    }

}
