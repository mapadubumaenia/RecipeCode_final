package com.RecipeCode.teamproject.reci.feed.recipes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.repository.IngredientRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.service.IngredientService;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.service.RecipeTagService;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final RecipesRepository recipesRepository;
    private final IngredientService ingredientService;
    private final RecipeContentService recipeContentService;
    private final RecipeTagService recipeTagService;
    private final IngredientRepository ingredientRepository;
    private final RecipeContentRepository recipeContentRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final MemberRepository memberRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;
    @PersistenceContext
    private EntityManager em; // 👉 JPA 영속성 컨텍스트 제어용

    // 내 팔로우 페이지 : 특정 ID 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
//        공개 레시피
        String status = "PUBLIC";

        Page<Recipes> recipesPage = recipesRepository
                .findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
                        followIds, status, pageable);

        return recipesPage.map(recipesDto -> recipeMapStruct.toRecipeDto(recipesDto));
    }

    /* 저장 */
    @Transactional
    public String createRecipe(RecipesDto recipesDto,
                               List<IngredientDto> ingredientDtos,
                               List<RecipeContentDto> contentDtos,
                               List<byte[]> images,
                               List<TagDto> tagDtos,
                               byte[] thumbnail,
                               String thumbnailUrlIgnored,
                               String userEmail) {

        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.unauthorized")));

        // 1) 레시피 엔티티 변환 및 기본값 설정
        Recipes recipe = recipeMapStruct.toRecipeEntity(recipesDto);
        String uuid = UUID.randomUUID().toString();
        recipe.setUuid(uuid);
        recipe.setMember(member);

        if ("VIDEO".equalsIgnoreCase(recipesDto.getRecipeType())) {
            recipe.setRecipeType("VIDEO");
            recipe.setVideoUrl(recipesDto.getVideoUrl());
            // 동영상은 내부 썸네일/다운로드 URL 불필요
            recipe.setThumbnail(null);
            recipe.setThumbnailUrl(recipesDto.getVideoUrl()); // 피드 썸네일 쓰려면(선택)
        } else {
            recipe.setRecipeType("IMAGE");
            recipe.setVideoUrl(null);
            recipe.setThumbnail(thumbnail);
            recipe.setThumbnailUrl(generateDownloadUrl(uuid));
        }

        // 3) 본문/카운터 기본값 보정 (선택)
        //  └ 엔티티가 기본형 long 이면 생략 가능
        if (recipe.getViewCount() == null) recipe.setViewCount(0L);
        if (recipe.getLikeCount() == null) recipe.setLikeCount(0L);
        if (recipe.getCommentCount() == null) recipe.setCommentCount(0L);
        if (recipe.getReportCount() == null) recipe.setReportCount(0L);


        // 2) 레시피 저장
        Recipes savedRecipe = recipesRepository.saveAndFlush(recipe);

        // 단계 저장: VIDEO일 땐 이미지 리스트 비워서 넘기면 됨
        List<byte[]> imgBytes = "VIDEO".equalsIgnoreCase(recipesDto.getRecipeType()) ? List.of() : images;
        recipeContentService.saveRecipeContent(contentDtos, imgBytes, recipe);

        // 3) 연관 엔티티 저장
        ingredientService.saveAll(ingredientDtos, recipe);
//        recipeContentService.saveRecipeContent(contentDtos, images, recipe);
        recipeTagService.saveTagsForRecipe(tagDtos, recipe);

        return savedRecipe.getUuid();
    }

    public String generateDownloadUrl(String uuid) {
        try {
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()          // http://localhost:8080
                    .path("/recipes/download")         // /recipes/download
                    .queryParam("uuid", uuid)          // ?uuid=...
                    .toUriString();
        } catch (IllegalStateException e) {
            // 요청 컨텍스트가 없으면 상대경로로 폴백 (테스트/배치 안전)
            return "/recipes/download?uuid=" + uuid;
        }
    }

    /* 수정 */
    @Transactional
    public void updateRecipe(String uuid,
                             RecipesDto recipesDto,
                             List<IngredientDto> ingredientDtos,
                             List<RecipeContentDto> contentDtos,
                             List<byte[]> images,
                             List<TagDto> tagDtos,
                             byte[] thumbnail,
                             String userEmail) {
        Recipes recipe = recipesRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

//        작성자 검증 : 테스트 후 살릴 것
        if (userEmail != null &&
                !userEmail.equalsIgnoreCase(recipe.getMember().getUserEmail())) {
            throw new RuntimeException(errorMsg.getMessage("errors.unauthorized"));
        }

        // 1) 레시피 기본 정보 업데이트
        recipeMapStruct.updateRecipe(recipesDto, recipe);

//        IMAGE / VIDEO 전환 처리
        if ("VIDEO".equalsIgnoreCase(recipesDto.getRecipeType())) {
            recipe.setRecipeType("VIDEO");
            recipe.setVideoUrl(recipesDto.getVideoUrl());
            recipe.setThumbnail(null);
            String youtubeThumb = toYoutubeEmbed(recipesDto.getVideoUrl());
            recipe.setThumbnailUrl(youtubeThumb); // 이미지 썸네일로 사용
        } else {
            recipe.setRecipeType("IMAGE");
            recipe.setVideoUrl(null);
            if (thumbnail != null && thumbnail.length > 0) {
                recipe.setThumbnail(thumbnail);
                recipe.setThumbnailUrl(generateDownloadUrl(uuid));
            }
        }

        // 2) 하위 엔티티 전체 교체
        // 2-1) 재료
        ingredientService.replaceAll(ingredientDtos, recipe);
        // 2-2) 조리 단계
        recipeContentService.updateRecipeContents(recipe, contentDtos, images);

        // 🔥 기존 태그 삭제 후 새로 추가
        recipeTagService.syncTagsForRecipe(recipe, tagDtos);

    }

    /* 상세 조회 */
    @Transactional(readOnly = true)
    public RecipesDto getRecipeDetails(String uuid) {
        Recipes recipe = recipesRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(
                        errorMsg.getMessage("errors.not.found")
                ));

        // 재료/단계는 단방향이라 개별 repo 조회
        var ingredients = ingredientRepository
                .findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(uuid);
        var contents = recipeContentRepository
                .findByRecipesUuidOrderByStepOrderAsc(uuid);

        RecipesDto dto = recipeMapStruct.toRecipeDto(recipe);
        dto.setIngredients(recipeMapStruct.toIngredientDtoList(ingredients));
        dto.setContents(recipeMapStruct.toRecipeContentDtoList(contents));
        return dto;


    }


    public void updateRecipe(String uuid,
                             RecipesDto recipesDto,
                             List<IngredientDto> ingredientDtos,
                             List<RecipeContentDto> contentDtos,
                             List<byte[]> images,
                             List<TagDto> tagDtos) {
        updateRecipe(uuid, recipesDto, ingredientDtos, contentDtos, images,
                tagDtos, null, null);
    }

    //    상세조회
    public Recipes findById(String uuid) {
        return recipesRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
    }

    /* 삭제 */
//    @Transactional
//    public void deleteRecipe(String uuid) {
//        // 부모에 재료/단계 컬렉션 안 들고 있으니 FK 제약 피하려면 수동 삭제 필요
//        recipeTagRepository.deleteByRecipesUuid(uuid);
//        ingredientRepository.deleteByRecipesUuid(uuid);
//        recipeContentRepository.deleteByRecipesUuid(uuid);
//        recipesRepository.deleteById(uuid);
//
//    }

    /* 소프트 삭제 */
    @Transactional
    public void softDeleteRecipe(String uuid) {
        Recipes recipes = recipesRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        if (Boolean.TRUE.equals(recipes.isDeleted())) return;  // 이미 삭제면 무시
        recipes.setDeleteDate(LocalDateTime.now()); // 삭제 시각 기록 (원하면)
        recipesRepository.delete(recipes);

    }

    public byte[] findThumbnailByUuid(String uuid) {
        Recipes recipes = recipesRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return recipes.getThumbnail();
    }

    /* 조회수 */
    public void increaseViewCount(String uuid) {
        Recipes recipe = recipesRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        recipe.setViewCount(recipe.getViewCount() + 1);
    }


    public String toYoutubeEmbed(String url) {
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
                java.util.Map<String, String> params = splitQuery(q);
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
        } catch (Exception ignore) {
        }
        return null;
    }

    private java.util.Map<String, String> splitQuery(String query) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        if (query == null) return map;
        for (String p : query.split("&")) {
            int i = p.indexOf('=');
            if (i > 0) map.put(p.substring(0, i), p.substring(i + 1));
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


    public void deleteRecipe(String uuid) {
    }
}
