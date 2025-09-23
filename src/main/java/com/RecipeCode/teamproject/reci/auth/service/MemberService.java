package com.RecipeCode.teamproject.reci.auth.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.admin.repository.AdminRepository;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.auth.membertag.repository.MemberTagRepository;
import com.RecipeCode.teamproject.reci.auth.notisetting.entity.NotiSetting;
import com.RecipeCode.teamproject.reci.auth.notisetting.repository.NotiSettingRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final NotiSettingRepository notiSettingRepository;
    private final MapStruct mapStruct;
    private final PasswordEncoder passwordEncoder;
    private final ErrorMsg errorMsg;
    private final MemberTagRepository memberTagRepository;
    private final TagService tagService;


//   회원가입
    public void save(MemberDto memberDto) {
//        중복이메일 검사
        if (memberRepository.existsById(memberDto.getUserEmail())) {
            throw new RuntimeException((errorMsg.getMessage("errors.register")));
        }

//        비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        Member member = mapStruct.toEntity(memberDto);
        member.setPassword(encodedPassword);
        if (member.getProfileStatus() == null || member.getProfileStatus().isBlank()) {
            member.setProfileStatus("PUBLIC");
            member.setProvider("local");
            member.setRole("R_USER");
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
        }
    }

//    상세조회(Profile 페이지용)
    public Member getByUserEmail(String email) {
        return memberRepository.findByUserEmail(email)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.register")));

    }


    public Member getByUserEmailTags(String email) {
        return memberRepository.findByUserEmailWithTags(email)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

    }


//    프로필 업데이트
    @Transactional
    public void updateProfile(MemberDto memberDto) {
        Member member = memberRepository.findByUserEmail((memberDto.getUserEmail()))
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        member.setNickname(memberDto.getNickname());
        member.setUserLocation(memberDto.getUserLocation());
        member.setUserIntroduce(memberDto.getUserIntroduce());
        member.setUserWebsite(memberDto.getUserWebsite());
        member.setUserInsta(memberDto.getUserInsta());
        member.setUserYoutube(memberDto.getUserYoutube());
        member.setUserBlog(memberDto.getUserBlog());
        member.setProfileStatus(memberDto.getProfileStatus());

        MultipartFile file = memberDto.getProfileImage();
        if (file != null && !file.isEmpty()) {
            try {
                member.setProfileImage(file.getBytes());

                // URL은 컨트롤러 엔드포인트로 생성
                String imageUrl = "/member/" + member.getUserId() + "/profile-image";
                member.setProfileImageUrl(imageUrl);
                // 옵션: profileImageUrl 생성/저장 로직 여기에 추가
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 처리 실패", e);
            }
        }

        List<TagDto> tagDtos = memberDto.getInterestTags();
        replaceTags(member, tagDtos);

        List<NotiSetting> settings = notiSettingRepository.findByMember(member);
        for (NotiSetting s : settings) {
            if ("FOLLOW".equals(s.getTypeCode())) {
                s.setAllow(Boolean.TRUE.equals(memberDto.getNoti_FOLLOW()));
            }
            if ("COMMENT".equals(s.getTypeCode())) {
                s.setAllow(Boolean.TRUE.equals(memberDto.getNoti_COMMENT()));
            }
        }
        memberRepository.save(member);
    }

    private void replaceTags(Member member, List<TagDto> tagDtos) {
        memberTagRepository.deleteByMember_UserEmail(member.getUserEmail()); // repository에 추가 메서드 필요
        member.getMemberTags().clear();

        // 2) 들어온 태그들을 Tag 엔티티로 변환 및 연결
        if (tagDtos != null) {
            for (com.RecipeCode.teamproject.reci.tag.dto.TagDto dto : tagDtos) {
                String tagName = dto.getTag().trim();
                if (tagName.isBlank()) continue;
                Tag tag = tagService.saveOrGetTag(tagName); // 존재하면 재사용, 없으면 생성
                MemberTag mt = new MemberTag();
                mt.setMember(member);
                mt.setTag(tag);
                member.getMemberTags().add(mt);
                memberTagRepository.save(mt);
            }
        }
    }
}
