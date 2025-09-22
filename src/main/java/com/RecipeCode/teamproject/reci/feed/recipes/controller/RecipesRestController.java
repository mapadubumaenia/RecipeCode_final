package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecipesRestController {

    private final RecipesService recipesService;

    @DeleteMapping("/recipes/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable String uuid,
                                       @AuthenticationPrincipal SecurityUserDto user) {
        recipesService.softDeleteRecipe(uuid);
        return ResponseEntity.noContent().build();
    }


}
