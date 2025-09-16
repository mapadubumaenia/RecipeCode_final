package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.service.UserDetailsServiceImpl;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RecipesViewController {
    private final RecipesService recipesService;
    private final RecipeContentService recipeContentService;


    /* 레시피 등록 폼 이동 */
    @GetMapping("/recipes/add")
    public String createForm() {
        return "feed/recipe_add"; // JSP or Thymeleaf 템플릿
    }

    /* 레시피 수정 폼 이동 */
    @GetMapping("/recipes/{uuid}/edit")
    public String editForm(@PathVariable String uuid, Model model) {
        RecipesDto dto = recipesService.getRecipeDetails(uuid);
        model.addAttribute("recipe", dto);
        return "feed/recipe_add";
    }


    /** 레시피 등록*/
    @PostMapping(path = "/recipes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRecipe(
            @ModelAttribute RecipesDto recipesDto,                        // ingredients[i].*, contents[i].*, tags[i]
            @RequestParam(value="thumbnail", required=false) MultipartFile thumbnail,
            @RequestParam(value="stepImages", required=false) List<MultipartFile> stepImages,
            @AuthenticationPrincipal SecurityUserDto userDetails // ✅ 현재 로그인한 유저
    ) throws Exception {

//        TODO: 개발용 테스트유저 : 다되면 지울것!!
//        String userEmail = (userDetails != null) ? userDetails.getUsername() : "asdf1234@naver.com";

        byte[] thumbnailBytes = (thumbnail != null && !thumbnail.isEmpty()) ? thumbnail.getBytes() : null;

        List<byte[]> images = new ArrayList<>();
        if (stepImages != null) for (MultipartFile f : stepImages)
            if (f != null && !f.isEmpty()) images.add(f.getBytes());

        // tags(List<String>) → TagDto
        List<TagDto> tagDtos = (recipesDto.getTags() != null) ? recipesDto.getTags() : new ArrayList<>();

        // sortOrder/stepOrder 보정
        List<IngredientDto> ingredientDtos =
                recipesDto.getIngredients() != null ? recipesDto.getIngredients() : new ArrayList<>();
        for (int i = 0; i < ingredientDtos.size(); i++)
            if (ingredientDtos.get(i).getSortOrder() == null) ingredientDtos.get(i).setSortOrder((long)(i+1));

        List<RecipeContentDto> contentDtos =
                recipesDto.getContents() != null ? recipesDto.getContents() : new ArrayList<>();
        for (int i = 0; i < contentDtos.size(); i++)
            if (contentDtos.get(i).getStepOrder() == null) contentDtos.get(i).setStepOrder((long)(i+1)); // 프로젝트 필드명에 맞게

        // TODO: 로그인 붙이면 userEmail -> userDetails.getUsername() 수정할 것
        String uuid = recipesService.createRecipe(
                recipesDto, ingredientDtos, contentDtos, images, tagDtos, thumbnailBytes,
                recipesDto.getThumbnailUrl(), // 없으면 null
                userDetails.getUsername()
        );
        return "redirect:/recipes/" + uuid;
    }

    /* 수정 */
    @PutMapping(value = "/recipes/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateRecipe(
            @PathVariable String uuid,
            @ModelAttribute RecipesDto recipesDto,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "stepImages", required = false) List<MultipartFile> stepImages,
            @AuthenticationPrincipal SecurityUserDto userDetails
    ) throws Exception {

        //        TODO: 개발용 테스트유저 : 다되면 지울것!!
//        String userEmail = (userDetails != null) ? userDetails.getUsername() : "test@test.com";

//        유저 조회 : 테스트 후 살릴 것
        Member member = new Member();
        member.setUserEmail(userDetails.getUsername());

        byte[] thumbnailBytes = (thumbnail != null && !thumbnail.isEmpty()) ? thumbnail.getBytes() : null;

        List<byte[]> images = new ArrayList<>();
        if (stepImages != null){
            for(MultipartFile f : stepImages) {
                if(f != null && !f.isEmpty()) images.add(f.getBytes());
            }
        }

        List<TagDto> tagDtos = (recipesDto.getTags() != null) ? recipesDto.getTags() : new ArrayList<>();
        List<IngredientDto> ingredientDtos = (
                recipesDto.getIngredients() != null) ? recipesDto.getIngredients() : new ArrayList<>();
                for (int i = 0; i < ingredientDtos.size(); i++) {
                    if (ingredientDtos.get(i).getSortOrder() == null) ingredientDtos.get(i).setSortOrder((long)(i+1));
                }

        List<RecipeContentDto> contentDtos = (recipesDto.getContents() != null) ? recipesDto.getContents() : new ArrayList<>();
                for (int i = 0; i < contentDtos.size(); i++) {
                    if(contentDtos.get(i).getStepOrder() == null) contentDtos.get(i).setStepOrder((long)(i+1));
                }

//           TODO: 로그인 붙이면 userEmail -> userDetails.getUsername() 수정할 것
        recipesService.updateRecipe(
                uuid, recipesDto, ingredientDtos, contentDtos, images, tagDtos,
                thumbnailBytes, userDetails.getUsername()
        );

        return "redirect:/recipes/" + uuid;
    }


    @GetMapping("recipes/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadThumbnail(@RequestParam("uuid") String uuid) {
        Recipes recipes = recipesService.findById(uuid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment",recipes.getUuid());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(recipes.getThumbnail(),
                headers, HttpStatus.OK);
    }

    // 단계 이미지
    @GetMapping(value = "/recipes/content/download")
    public ResponseEntity<byte[]> downloadStep(@RequestParam Long stepId) {
        RecipeContent recipeContent = recipeContentService.findById(stepId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", recipeContent.getStepId().toString());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(recipeContent.getRecipeImage(),
                headers, HttpStatus.OK);
    }

    /** ✅ 상세 페이지 (리다이렉트 목적지) */
    @GetMapping("/recipes/{uuid}")
    public String detail(@PathVariable String uuid, Model model) {
        // 조회수 +1
         recipesService.increaseViewCount(uuid);

        // 서비스에서 DTO로 가져와 뷰에 그대로 바인딩
        RecipesDto dto = recipesService.getRecipeDetails(uuid); // 서비스에 맞게 메서드명 조정

        // 업로드 시간 문자열
        String insertTime = "";
        if (dto.getInsertTime() != null) {
            insertTime = dto.getInsertTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        }

        // 이미지 영상 분기 + 임베드 url 만들기
        boolean isVideo = "VIDEO".equalsIgnoreCase(dto.getRecipeType());
        String embedUrl = null;
        if (isVideo && dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            embedUrl = recipesService.toYoutubeEmbed(dto.getVideoUrl()); // 유튜브면 embed로 변환
            if (embedUrl == null) {
                // 유튜브가 아니면 일단 원본 URL로 시도 (일부 사이트는 X-Frame-Options로 막힐 수 있음)
                embedUrl = dto.getVideoUrl();
            }
        }


        model.addAttribute("recipe", dto);
        model.addAttribute("embedUrl", embedUrl);
        model.addAttribute("isVideo", isVideo);
        model.addAttribute("insertTime", insertTime);
        return "feed/recipe_details"; // JSP 경로
    }



}
