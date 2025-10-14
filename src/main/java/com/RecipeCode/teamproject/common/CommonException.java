package com.RecipeCode.teamproject.common;


import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@ControllerAdvice
public class CommonException {
    // TODO: ajax용 에러 처리:ResponseStatusException 에러가 발생하면 이 함수 실행됨
    // TODO: ResponseEntity 클래스 목적: jsp 상태코드(ok.not_found404 등) + 데이터(품질향상)
    //   사용법: ResponseEntity
    //          .status()
    //          .body();
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatusCode())  //409(에러)
                .body(e.getReason());       //에러내용
    }


    //  컨트롤러에서 어떤 에러가 발생하더라도 이 함수가 실행됨
    @ExceptionHandler(Exception.class)
    public String internalServerErrorException(Exception e
            , Model model
    ) {
        String errors = e.getMessage();       // 에러 내용
        log.info("에러: " + errors);
        model.addAttribute("errors", errors); // 에러를 모델에 담기

        return "errors";                      // jsp명
    }

    // 좋아요 본인 제한 에러
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException e , Model model){
        if(e.getMessage().contains("로그인")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // UNAUTHORIZED 401
                    .body(e.getMessage());
        }

        if (e.getMessage().contains("관리자")) {
            String errors = e.getMessage();
            model.addAttribute("errors", errors);
            return "errors";
        }

        return ResponseEntity
                // BAD_REQUEST(에러) 400
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}