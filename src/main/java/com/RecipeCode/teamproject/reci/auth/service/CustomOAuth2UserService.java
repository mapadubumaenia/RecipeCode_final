package com.RecipeCode.teamproject.reci.auth.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.notisetting.entity.NotiSetting;
import com.RecipeCode.teamproject.reci.auth.notisetting.repository.NotiSettingRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final NotiSettingRepository notiSettingRepository;
    private final ErrorMsg errorMsg;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 서비스
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        // provider 정보
        String provider = userRequest.getClientRegistration().getRegistrationId();// google, kakao
        String providerId = null;
        String email = null;
        String nickname = null;
        String profileImageUrl = null;
        // attributes (구글/카카오 사용자 정보)
        if ("google".equals(provider)) {
            email = oAuth2User.getAttribute("email");
            nickname = oAuth2User.getAttribute("name");
            profileImageUrl = oAuth2User.getAttribute("picture");
            providerId = oAuth2User.getAttribute("sub"); // 구글 고유 ID
        } else if ("kakao".equals(provider)) {
            //unchecked cast 경고 방지
            Object kakaoAccountObj = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> kakaoAccount = Collections.emptyMap();
            if (kakaoAccountObj instanceof Map) {
                kakaoAccount = (Map<String, Object>) kakaoAccountObj;
            }

            Object profileObj = kakaoAccount.get("profile");
            Map<String, Object> profile = Collections.emptyMap();
            if (profileObj instanceof Map) {
                profile = (Map<String, Object>) profileObj;
            }
            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
            profileImageUrl = (String) profile.get("profile_image_url");

            Object kakaoIdObj = oAuth2User.getAttribute("id");
            providerId = kakaoIdObj != null ? String.valueOf(kakaoIdObj) : null;
        }
        log.info("providerId class = {}", providerId.getClass().getName());

        // DB에서 회원 조회
        Member member = memberRepository.findByUserEmail(email).orElse(null);

        if (member == null) {
            byte[] profileImage = null;
            if (profileImageUrl != null) {
                profileImage = downloadImageAsBytes(profileImageUrl); // 🔹 여기서 변환
            }

            // 기본 userId 후보 생성
            String baseUserId = nickname != null
                    ? (nickname.startsWith("@") ? nickname : "@" + nickname)
                    : "@" + provider + "_" + providerId;

            // 중복되면 숫자 붙여서 보정
            String uniqueUserId = baseUserId;
            int count = 1;
            while (memberRepository.existsByUserId(uniqueUserId)) {
                uniqueUserId = baseUserId + count;
                count++;
            }

            // 최초 로그인 → 회원 가입 처리
            member = Member.builder()
                    .userEmail(email)
                    .userId(uniqueUserId)
                    .nickname(nickname != null ? nickname : provider + "_" + providerId)
                    .profileImage(profileImage)
                    .profileStatus("PUBLIC")
                    .role("ROLE_USER")
                    .provider(provider)
                    .providerId(providerId)
                    .deleted("N")
                    .build();
            memberRepository.save(member);
            memberRepository.flush();

            NotiSetting follow = NotiSetting.builder()
                    .member(member)
                    .typeCode("FOLLOW")
                    .allow(true)
                    .build();

            NotiSetting comment = NotiSetting.builder()
                    .member(member)
                    .typeCode("COMMENT")
                    .allow(true)
                    .build();

            notiSettingRepository.save(follow);
            notiSettingRepository.save(comment);


        } else {
            // 기존 회원일시 프로필 정보 업데이트
            member.setProvider(provider);
            member.setProviderId(providerId);
            memberRepository.save(member);
        }

        // 탈퇴 계정이면 차단
        if ("Y".equals(member.getDeleted())) {
            throw new RuntimeException(errorMsg.getMessage("errors.deleted"));
        }

        return new SecurityUserDto(member,
                Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                oAuth2User.getAttributes());
    }


    private byte[] downloadImageAsBytes(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
