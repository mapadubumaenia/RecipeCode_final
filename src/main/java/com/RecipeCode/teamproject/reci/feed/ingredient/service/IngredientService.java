package com.RecipeCode.teamproject.reci.feed.ingredient.service;


import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.ingredient.repository.IngredientRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;

//    상세조회
    public List<IngredientDto> getIngredients(String recipesUuid) {
        List<Ingredient> ingredients = ingredientRepository
                .findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(recipesUuid);

        if (ingredients.isEmpty()) {
            throw new RuntimeException(errorMsg.getMessage("errors.not.found"));
        }

        return recipeMapStruct.toIngredientDtoList(ingredients);
    }

//    저장(레시피와 함께 저장)
    public void saveAll(List<IngredientDto> ingredientDtos,
                        Recipes recipe) {
        for (int i = 0; i < ingredientDtos.size(); i++) {
        Ingredient ingredient = recipeMapStruct.toIngredientEntity(ingredientDtos.get(i));

            ingredient.setSortOrder((i+1L)*10);      // 10 단위로 자동 부여
            ingredient.setRecipes(recipe);           // 레시피 연관 관계

            ingredientRepository.save(ingredient);
        }
    }

//    전체교체용?
    public void replaceAll(List<IngredientDto> ingredientDtos, Recipes recipe) {
        ingredientRepository.deleteByRecipesUuid(recipe.getUuid());
        saveAll(ingredientDtos, recipe);
    }

//    수정 (재료명/분량만 수정)
    public void updateIngredient(IngredientDto ingredientDto) {
        Ingredient ingredient = ingredientRepository.findById(ingredientDto.getId())
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));

        recipeMapStruct.updateIngredient(ingredientDto, ingredient);
    }

}
