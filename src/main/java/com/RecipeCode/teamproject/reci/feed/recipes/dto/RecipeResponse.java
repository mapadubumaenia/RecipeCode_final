package com.RecipeCode.teamproject.reci.feed.recipes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// @Data : getter/setter/toString 자동으로 생성
// (DTO/Entity에 자주 쓰는 메서드를 한 번에 다 만들어줌)
// getter (모든 필드) setter (모든 필드) toString() equals() & hashCode()
// @AllArgsConstructor : 모든 필드를 받는 생성자 자동 생성, 생성자를 바로 쓸 수 있음
// @NoArgsConstructor : 기본 생성자(파라미터 없는 생성자) 자동 생성
// @RequiredArgsConstructor : final 붙은 필드만 받는 생성자 자동 생성
// TODO : 단점이 있음. (무분별한 setter 생성, equals & hashCode 자동 생성, toString 순환 참조 문제)
//  무분별한 사용 금지!

@Data
@AllArgsConstructor
public class RecipeResponse {
    private String uuid;
    private String thumbnailUrl;

//    fileDb 실습버전:
//    public void save(){} : DB 저장 후 프론트에서 별도 API 요청, 업로드 후 새로고침
//    현재 버전 : 저장하면서 동시에 uuid, thumbnailUrl 바로 응답.
//    프론트는 그 값으로 <img src="..."> 즉시 띄울 수 있음.

}
