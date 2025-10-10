package com.RecipeCode.teamproject.reci.function.emailCertify.controller;

import com.RecipeCode.teamproject.reci.function.emailCertify.dto.ApiResponse;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.ResetPasswordRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.SendEmailRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.VerifyCodeRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.service.EmailCertifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-certify")
@RequiredArgsConstructor
public class EmailCertifyController {
    private final EmailCertifyService emailCertifyService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendCode(@RequestBody SendEmailRequest dto) {
        emailCertifyService.sendCertificationCode(dto);
        try {
            emailCertifyService.sendCertificationCode(dto);
            return ResponseEntity.ok(new ApiResponse("ok", "인증 메일 발송 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(@RequestBody VerifyCodeRequest dto) {
        try {
            boolean valid = emailCertifyService.verifyCode(dto);
            if (valid) {
                return ResponseEntity.ok(new ApiResponse("ok", "인증 성공"));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse("error", "잘못된 인증번호입니다."));
            }
        } catch (IllegalStateException e) {
            // 예외 메시지를 그대로 클라이언트에 전달 ("이미 사용된 인증번호입니다.", "만료된 인증번호입니다." 등)
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse("error", "인증 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest dto) {
        try {
            emailCertifyService.resetPassword(dto);
            return ResponseEntity.ok(new ApiResponse("ok", "비밀번호 변경 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse("error", e.getMessage()));
        }
    }
}
