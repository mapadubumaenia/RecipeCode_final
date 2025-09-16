package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecipesProfileController {

    private final RecipesService recipesService;



}
