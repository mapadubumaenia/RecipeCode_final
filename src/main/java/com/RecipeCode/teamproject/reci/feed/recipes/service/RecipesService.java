package com.RecipeCode.teamproject.reci.feed.recipes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipeResponse;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final MemberRepository memberRepository;
    private final RecipesRepository recipesRepository;
    private final TagRepository tagRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;
    private final RecipeContentService recipeContentService;

    // 내 팔로우 페이지 : 특정 ID 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
//        공개 레시피
        String status = "PUBLIC";

        Page<Recipes> recipesPage = recipesRepository
                .findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
                    followIds, status, pageable);

        return recipesPage.map(recipesDto -> mapStruct.toDto(recipesDto));
    }


//    레시피 등록
    @Transactional
    public RecipeResponse save(RecipesDto recipesDto,
                               String userEmail,
                               byte[] image,
                               List<MultipartFile> stepImages) throws Exception {

//      Dto확인 : 디버깅용
        System.out.println("여기 Service Dto"+recipesDto);
        System.out.println("여기 Service Contents"+recipesDto.getContents());

//        1) 작성자 Member 조회
        Member member = memberRepository.findById(userEmail)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

//        2) Dto -> Entity 변환
        Recipes recipes = mapStruct.toEntity(recipesDto);
        recipes.setMember(member);
        recipes.setLikeCount(0L);
        recipes.setCommentCount(0L);
        recipes.setReportCount(0L);
        recipes.setViewCount(0L);

//        2-1) UUID 만들기
        String newUuid = UUID.randomUUID().toString();
//        2-2) 다운로드 url 만들기
        String downloadURL=generateDownloadUrl(newUuid);
//        2-3) 레시피에 uuid url 저장(setter)
        recipes.setUuid(newUuid);
        recipes.setThumbnailUrl(downloadURL);
        recipes.setThumbnail(image);

        // 3) 재료 매핑
        if (recipes.getIngredients() != null) {
            recipes.getIngredients().forEach(ingredient -> {
                ingredient.setRecipes(recipes);
                if (ingredient.getSortOrder() == null) {
                    ingredient.setSortOrder(0L);
                }
            });
        }

//        4) 레시피 먼저 저장
        Recipes insert = recipesRepository.save(recipes);

//        5) 레시피 콘텐츠
        recipeContentService.saveStepContents(insert, recipesDto.getContents(), stepImages);

//        6) 태그 처리
        if(recipesDto.getTags() != null && !recipesDto.getTags().isEmpty()){
            for (String tagName : recipesDto.getTags()) {
//                1) 태그 중복 체크
                Tag tag = tagRepository.findByTag(tagName)
                        .orElseGet(()-> {
//                            없으면 새로 저장
                            Tag newTag = new Tag();
                            newTag.setTag(tagName);
                            return tagRepository.save(newTag);
                        });

//                2) 레시피-태그 관계 저장
                RecipeTag recipeTag = new RecipeTag();
                recipeTag.setRecipes(insert);
                recipeTag.setTag(tag);
                recipeTagRepository.save(recipeTag);
            }
        }

            return new RecipeResponse(insert.getUuid(),
                    insert.getThumbnailUrl());              // 새로 생성된 레시피 ID 반환
    }

//    다운로드 url을 만들어주는 메소드
//    http://localhost:8080/recipes/download?uuid=newUuid값
    public String generateDownloadUrl(String newUuid) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/recipes/download")            // .query(기존공부한거) 문자열끼리 붙임)
                .queryParam("uuid", newUuid)    // .queryParm(gpt추천) 자동으로 인코딩 처리 : 이상하면 다시 수정예정
                .toUriString();
    }


    public byte[] findThumbnailByUuid(String uuid) {
        Recipes recipes = recipesRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return recipes.getThumbnail();
    }

//    상세조회
    public RecipesDto findByUuid(String uuid) {
        Recipes recipe = recipesRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return mapStruct.toDto(recipe);
    }

}
