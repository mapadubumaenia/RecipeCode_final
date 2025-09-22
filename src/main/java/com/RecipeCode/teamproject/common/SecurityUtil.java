package com.RecipeCode.teamproject.common;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final ErrorMsg errorMsg;

    public SecurityUserDto getLoginUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        SecurityUserDto 유저인지 확인 -> 아니면 에러처리
//        TODO; 사용법: 변수 instanceof 클래스 변수2 : 변수가 클래스이면 true, 아니면 false
        if (principal instanceof SecurityUserDto user) {
            return user;
        }
        throw new RuntimeException(errorMsg.getMessage("errors.unauthorized"));
    }

    public String getLoginUserEmail() {
        return getLoginUser().getUsername(); // username = 이메일
    }
}
