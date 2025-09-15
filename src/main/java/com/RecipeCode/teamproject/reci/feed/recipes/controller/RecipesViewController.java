package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
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

    /** 레시피 등록 폼 이동 */
    @GetMapping("/recipes/add")
    public String createForm() {
        return "feed/recipe_add"; // JSP or Thymeleaf 템플릿
    }

    @PostMapping(path = "/recipes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRecipe(
            @ModelAttribute RecipesDto recipesDto,                        // ingredients[i].*, contents[i].*, tags[i]
            @RequestParam(value="thumbnail", required=false) MultipartFile thumbnail,
            @RequestParam(value="stepImages", required=false) List<MultipartFile> stepImages,
            Principal principal
    ) throws Exception {

        Member member = new Member();
        member.setUserEmail(principal != null ? principal.getName() : "leetonny@naver.com");

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

        String uuid = recipesService.createRecipe(
                recipesDto, ingredientDtos, contentDtos, images, tagDtos, thumbnailBytes,
                recipesDto.getThumbnailUrl(), // 없으면 null
                member
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
        // 선택: 조회수 +1
        // recipesService.increaseViewCount(uuid);

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
            embedUrl = toYoutubeEmbed(dto.getVideoUrl()); // 유튜브면 embed로 변환
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

    private String toYoutubeEmbed(String url){
        try {
            java.net.URL u = new java.net.URL(url);
            String host = u.getHost();
            String path = u.getPath();
            String q = u.getQuery(); // v=, t= 같은 파라미터

            // youtu.be/VIDEOID
            if (host.contains("youtu.be")) {
                String id = path.replaceFirst("^/", "");
                String start = parseStartSeconds(q);
                return start == null
                        ? "https://www.youtube.com/embed/" + id
                        : "https://www.youtube.com/embed/" + id + "?start=" + start;
            }
            // youtube.com/watch?v=VIDEOID
            if (host.contains("youtube.com")) {
                java.util.Map<String,String> params = splitQuery(q);
                String id = params.get("v");
                if (id != null && !id.isBlank()) {
                    String start = parseStartSeconds(q);
                    return start == null
                            ? "https://www.youtube.com/embed/" + id
                            : "https://www.youtube.com/embed/" + id + "?start=" + start;
                }
                // shorts/VIDEOID
                if (path.startsWith("/shorts/")) {
                    String ids = path.substring("/shorts/".length());
                    return "https://www.youtube.com/embed/" + ids;
                }
                // 재생목록
                if (path.startsWith("/playlist")) {
                    String list = splitQuery(q).get("list");
                    if (list != null) return "https://www.youtube.com/embed/videoseries?list=" + list;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    private java.util.Map<String,String> splitQuery(String query) {
        java.util.Map<String,String> map = new java.util.HashMap<>();
        if (query == null) return map;
        for (String p : query.split("&")) {
            int i = p.indexOf('=');
            if (i > 0) map.put(p.substring(0, i), p.substring(i+1));
        }
        return map;
    }

    private String parseStartSeconds(String query) {
        if (query == null) return null;
        // t=1m30s / t=90s / start=90 지원
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:(?:^|&)t=([^&]+))|(?:^|&)start=(\\d+)").matcher(query);
        if (!m.find()) return null;
        String t = m.group(1) != null ? m.group(1) : m.group(2);
        if (t == null) return null;
        if (t.matches("\\d+")) return t; // 초
        int secs = 0;
        java.util.regex.Matcher mh = java.util.regex.Pattern.compile("(\\d+)h").matcher(t);
        java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d+)m").matcher(t);
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("(\\d+)s").matcher(t);
        if (mh.find()) secs += Integer.parseInt(mh.group(1)) * 3600;
        if (mm.find()) secs += Integer.parseInt(mm.group(1)) * 60;
        if (ms.find()) secs += Integer.parseInt(ms.group(1));
        return secs > 0 ? String.valueOf(secs) : null;
    }

}
