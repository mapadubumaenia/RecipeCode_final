package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipeResponse;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class RecipesViewController {
    private final RecipesService recipesService;

//    업로드 페이지 열기
    @GetMapping("/recipes/addition")
    public String createRecipeView(){
        return "feed/recipe_add";
    }

////    업로드
//    @PostMapping("/recipes/add")
//    public void addRecipe(RecipesDto recipesDto,                          byte[] image) {
//        byte[] imageBytes = (image != null) ? image.getBytes():null;
//        String dummy = "dltkdals529@gmail.com"; // 임시유저
//
//        RecipeResponse saved = recipesService.save(recipesDto, dummy, imageBytes);
//
//        return "redirect:/recipe_details/"+saved.getUuid();
//    }
//
//  상세 페이지 열기
    @GetMapping("/recipes/{uuid}")
    public String detailRecipeView(@PathVariable("uuid") String uuid,
                                   Model model) {
        RecipesDto recipe = recipesService.findByUuid(uuid);
        model.addAttribute("recipe", recipe);
        return "feed/recipe_details";

    }

}
