package com.RecipeCode.teamproject.reci.feed.recipes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
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
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final RecipesRepository recipesRepository;
    private final IngredientService ingredientService;
    private final RecipeContentService recipeContentService;
    private final RecipeTagService recipeTagService;
    private final IngredientRepository ingredientRepository;
    private final RecipeContentRepository recipeContentRepository;
    private final RecipesLikesRepository  recipesLikesRepository;
    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final FollowRepository followRepository;
    private final ErrorMsg errorMsg;
    private final MapStruct mapStruct;


    // 내 팔로우 페이지 : 특정 ID 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
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
            // ▶ VIDEO 업로드: 썸네일은 항상 "이미지 URL"로 저장 (검색 카드에서 <img>로 사용)
            recipe.setRecipeType("VIDEO");
            recipe.setVideoUrl(recipesDto.getVideoUrl());
            recipe.setThumbnail(null);
            String embedUrl = toYoutubeEmbed(recipesDto.getVideoUrl());
            recipe.setThumbnailUrl(embedUrl);

            // 🔑 핵심 변경: embed를 thumbnailUrl에 넣지 않고, i.ytimg.com 이미지 URL을 넣는다
//            String thumb = youtubeThumb(recipesDto.getVideoUrl());
//            recipe.setThumbnailUrl(thumb != null ? thumb : "");

            // (선택) 상세에서 쓸 embed URL을 별도 필드로 관리한다면 여기서 세팅
            // recipe.setEmbedUrl(toYoutubeEmbed(recipesDto.getVideoUrl()));

        } else {
            // ▶ IMAGE 업로드
            recipe.setRecipeType("IMAGE");
            recipe.setVideoUrl(null);
            recipe.setThumbnail(thumbnail);
            recipe.setThumbnailUrl(generateDownloadUrl(uuid)); // 이미지 다운로드 URL
        }

        // 3) 본문/카운터 기본값 보정
        if (recipe.getViewCount() == null) recipe.setViewCount(0L);
        if (recipe.getLikeCount() == null) recipe.setLikeCount(0L);
        if (recipe.getCommentCount() == null) recipe.setCommentCount(0L);
        if (recipe.getReportCount() == null) recipe.setReportCount(0L);

        // 2) 레시피 저장
        Recipes savedRecipe = recipesRepository.saveAndFlush(recipe);

        // 단계 저장: VIDEO일 땐 이미지 리스트 비워서 넘기면 됨
        List<byte[]> imgBytes = "VIDEO".equalsIgnoreCase(recipesDto.getRecipeType()) ? List.of() : images;
        recipeContentService.saveRecipeContent(contentDtos, recipe);

        // 3) 연관 엔티티 저장
        ingredientService.saveAll(ingredientDtos, recipe);
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

        // 작성자 검증 : 테스트 후 살릴 것
        if (userEmail != null &&
                !userEmail.equalsIgnoreCase(recipe.getMember().getUserEmail())) {
            throw new RuntimeException(errorMsg.getMessage("errors.unauthorized"));
        }

        // 1) 레시피 기본 정보 업데이트
        recipeMapStruct.updateRecipe(recipesDto, recipe);

        // IMAGE / VIDEO 전환 처리
        if ("VIDEO".equalsIgnoreCase(recipesDto.getRecipeType())) {
            recipe.setRecipeType("VIDEO");
            recipe.setVideoUrl(recipesDto.getVideoUrl());
            recipe.setThumbnail(null);
            String embedUrl = toYoutubeEmbed(recipesDto.getVideoUrl());
            recipe.setThumbnailUrl(embedUrl);

            // 🔑 핵심 변경: 썸네일은 항상 "이미지 URL"로 저장
//            String thumb = youtubeThumb(recipesDto.getVideoUrl());
//            recipe.setThumbnailUrl(thumb != null ? thumb : "");

            // (선택) 상세 전용 embed 필드에 저장한다면 여기도 같이
            // recipe.setEmbedUrl(toYoutubeEmbed(recipesDto.getVideoUrl()));

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
        recipeContentService.updateRecipeContents(recipe, contentDtos);

        // 기존 태그 삭제 후 새로 추가
        recipeTagService.syncTagsForRecipe(recipe, tagDtos);
    }

    /* 상세 조회 */
    @Transactional
    public RecipesDto getRecipeDetails(String uuid, @Nullable String userEmail) {

        // 1) 레시피 + 작성자 불러오기
        Recipes recipe = recipesRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(
                        errorMsg.getMessage("errors.not.found")
                ));
        Member owner = recipe.getMember();

        // 2) DTO 매핑
        RecipesDto dto = recipeMapStruct.toRecipeDto(recipe);

        // 3) 좋아요 여부
        if (userEmail != null && ! userEmail.isBlank()) {
            memberRepository.findByUserEmail(userEmail).ifPresent(viewer ->{
                boolean l = recipesLikesRepository.existsByMemberAndRecipes(viewer, recipe);
                dto.setLiked(l);
            });
        } else {
            dto.setLiked(false);
        }

        // 4) 팔로우 여부
        boolean followingOwner = false;
        if (userEmail != null && ! userEmail.isBlank() && !userEmail.equals(owner.getUserEmail())) {
            Member viewer = memberRepository.findByUserEmail(userEmail)
                    .orElse(null);
            if(viewer != null) {
                followingOwner = followRepository.existsByFollowerAndFollowing(viewer, owner);
            }
        }
        dto.setFollowingOwner(followingOwner);


        // 재료/단계는 단방향이라 개별 repo 조회
        var ingredients = ingredientRepository
                .findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(uuid);
        var contents = recipeContentRepository
                .findByRecipesUuidOrderByStepOrderAsc(uuid);

        dto.setIngredients(recipeMapStruct.toIngredientDtoList(ingredients));
        dto.setContents(recipeMapStruct.toRecipeContentDtoList(contents));

        return dto;
    }

    // 비로그인/공용 진입용 편의 메서드
    @Transactional
    public RecipesDto getRecipeDetails(String uuid) {
        return getRecipeDetails(uuid, null);
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

    // 상세조회
    public Recipes findById(String uuid) {
        return recipesRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
    }

    /* 소프트 삭제 */
    @Transactional
    public void softDeleteRecipe(String uuid) {
        Recipes recipes = recipesRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        if (Boolean.TRUE.equals(recipes.isDeleted())) return;  // 이미 삭제면 무시
        recipes.setDeleteDate(LocalDateTime.now()); // 삭제 시각 기록 (원하면)
        // TODO : 물리 삭제 아님(entity @Where, @SQLDelete 사용)
        //   Hibernate가 soft delete로 변환 실행
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
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        recipe.setViewCount(recipe.getViewCount() + 1);
    }

    /* 코멘트수 */
    public void fillCommentCounts(List<RecipesDto> dtos) {
        if (dtos.isEmpty()) return;

        List<String> uuids = dtos.stream().map(RecipesDto::getUuid).distinct().toList();

        var rows = commentsRepository.countByRecipeUuids(uuids);
        var map = rows.stream().collect(Collectors.toMap(
                CommentsRepository.CommentCountView::getUuid,
                CommentsRepository.CommentCountView::getCnt
        ));
        dtos.forEach(dto -> dto.setCommentCount(map.getOrDefault(dto.getUuid(), 0L)));
    }

    // ---------------------------
    // 유튜브 관련 유틸
    // ---------------------------

    /** 상세페이지 iframe용: 원본 URL을 embed URL로 변환 */
    public String toYoutubeEmbed(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            String host = u.getHost();
            String path = u.getPath();
            String q = u.getQuery(); // v=, t= 같은 파라미터

            // youtu.be/VIDEOID
            if (host != null && host.contains("youtu.be")) {
                String id = path.replaceFirst("^/", "");
                String start = parseStartSeconds(q);
                return start == null
                        ? "https://www.youtube.com/embed/" + id
                        : "https://www.youtube.com/embed/" + id + "?start=" + start;
            }
            // youtube.com/watch?v=VIDEOID
            if (host != null && host.contains("youtube.com")) {
                java.util.Map<String, String> params = splitQuery(q);
                String id = params.get("v");
                if (id != null && !id.isBlank()) {
                    String start = parseStartSeconds(q);
                    return start == null
                            ? "https://www.youtube.com/embed/" + id
                            : "https://www.youtube.com/embed/" + id + "?start=" + start;
                }
                // shorts/VIDEOID
                if (path != null && path.startsWith("/shorts/")) {
                    String ids = path.substring("/shorts/".length());
                    return "https://www.youtube.com/embed/" + ids;
                }
                // 재생목록
                if (path != null && path.startsWith("/playlist")) {
                    String list = splitQuery(q).get("list");
                    if (list != null) return "https://www.youtube.com/embed/videoseries?list=" + list;
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /** 검색 카드용 썸네일: 유튜브 영상이면 i.ytimg.com 이미지 URL 반환 */
    private String youtubeThumb(String url) {
        if (url == null || url.isBlank()) return null;
        String id = extractYouTubeId(url);
        return (id == null) ? null : "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
    }

    /** 다양한 유튜브 URL에서 videoId 추출 (watch, youtu.be, shorts, embed 등) */
    private String extractYouTubeId(String url) {
        if (url == null || url.isBlank()) return null;
        java.util.regex.Matcher m;
        // watch?v=
        m = java.util.regex.Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        // youtu.be/<id>
        m = java.util.regex.Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        // /shorts/<id> 또는 /embed/<id>
        m = java.util.regex.Pattern.compile("/(shorts|embed)/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(2);
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
