package com.RecipeCode.teamproject.reci.feed.recipecontent.controller;

import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecipeContentController {

    private final RecipeContentService recipeContentService;

    @GetMapping("/recipes/content/download")
    public ResponseEntity<byte[]> downloadRecipeContent(@RequestParam Long stepId){
        byte[] image = recipeContentService.findRecipeImage(stepId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }




}
