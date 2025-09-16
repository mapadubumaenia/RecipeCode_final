package com.RecipeCode.teamproject.reci.feed.recipes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecipesRestController {

//    private final RecipesService recipesService;
//    private final RecipeContentRepository recipesContentRepository;
//    private final ErrorMsg errorMsg;
//
//    @PostMapping(value = "/recipes", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
//    public RecipeResponse save(@RequestPart("recipesDto") RecipesDto recipesDto,
//                               @RequestPart(value = "image", required = false)MultipartFile image
//                               ) throws Exception {
//        System.out.println("여기 Controller DTO:" + recipesDto);
//        System.out.println("여기 Controller Contents:" + recipesDto.getContents());
//        byte[] imageBytes = (image != null) ? image.getBytes() : null;
//        String dummyEmail = "dltkdals529@gmail.com"; // 임시유저
//        return recipesService.save(recipesDto, dummyEmail, imageBytes);
//    }
//    public ResponseEntity<?> save(@RequestPart("recipesDto") RecipesDto recipesDto,
//                                  @RequestPart(value = "image", required = false) MultipartFile thumbnail,
//                                  @RequestPart(value = "stepImages", required = false)List<MultipartFile> stepImages,
//                                  String userEmail) throws Exception{
////        Dto 확인 : 디버깅용
//        System.out.println("여기 Controller DTO:" + recipesDto);
//        System.out.println("여기 Controller Contents:" + recipesDto.getContents());
//
//        RecipeResponse response = recipesService.save(recipesDto, userEmail, thumbnail !=null ? thumbnail.getBytes() : null, stepImages);
//
//        return ResponseEntity.ok(response);
//    }
//
////    다운로드 url 만들기
//    @GetMapping("/recipes/download")
//    public ResponseEntity<byte[]> download(@RequestParam(defaultValue = "") String uuid) {
////      uuid 안 보냈을 때 400 으로 응답
//        if (uuid.isBlank()){
//            return ResponseEntity.badRequest().build(); // 400 Bad Request
//        }
//        byte[] image = recipesService.findThumbnailByUuid(uuid);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.IMAGE_JPEG);
//        return  new ResponseEntity<byte[]>(image, headers, HttpStatus.OK);
//    }



}
