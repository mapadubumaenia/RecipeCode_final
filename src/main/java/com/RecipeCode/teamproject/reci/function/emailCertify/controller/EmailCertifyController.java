package com.RecipeCode.teamproject.reci.function.emailCertify.controller;

import com.RecipeCode.teamproject.reci.function.emailCertify.dto.ApiResponse;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.ResetPasswordRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.SendEmailRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.VerifyCodeRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.service.EmailCertifyService;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse sendCode(@RequestBody SendEmailRequest dto) {
        emailCertifyService.sendCertificationCode(dto);
        return new ApiResponse("ok", "인증 메일 발송 완료");
    }

    @PostMapping("/verify")
    public ApiResponse verify(@RequestBody VerifyCodeRequest dto) {
        boolean valid = emailCertifyService.verifyCode(dto);
        return valid
                ? new ApiResponse("ok", "인증 성공")
                : new ApiResponse("error", "인증 실패");
    }

    @PostMapping("/reset")
    public ApiResponse resetPassword(@RequestBody ResetPasswordRequest dto) {
        emailCertifyService.resetPassword(dto);
        return new ApiResponse("ok", "비밀번호 변경 성공");
    }
}
