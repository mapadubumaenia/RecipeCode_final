package com.RecipeCode.teamproject.reci.function.emailCertify.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.ResetPasswordRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.SendEmailRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.dto.VerifyCodeRequest;
import com.RecipeCode.teamproject.reci.function.emailCertify.entity.EmailCertify;
import com.RecipeCode.teamproject.reci.function.emailCertify.repository.EmailCertifyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailCertifyService {
    private final EmailCertifyRepository emailCertifyRepository;
    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    /** 인증 메일 발송 */
    @Transactional
    public void sendCertificationCode(SendEmailRequest dto) {
        Member member = memberRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));


        // 기존 토큰 모두 무효화
        emailCertifyRepository.findTopByMemberOrderByTokenIdDesc(member)
                .ifPresent(oldToken -> {
                    oldToken.setUsed("Y");
                    emailCertifyRepository.save(oldToken);
                });

        String code = generate6DigitCode();

        EmailCertify token = EmailCertify.builder()
                .member(member)
                .code(code)
                .expiryAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10분 후 만료
                .used("N")
                .attempts(0)
                .build();

        emailCertifyRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getEmail());
        message.setSubject("[RecipeCode] 비밀번호 재설정 인증번호");
        message.setText("비밀번호 재설정 인증번호는 " + code + " 입니다. (10분 내 사용)");
        mailSender.send(message);
    }

    /** 인증번호 검증 */
    @Transactional(readOnly = true)
    public boolean verifyCode(VerifyCodeRequest dto) {
        Member member = memberRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        EmailCertify token = emailCertifyRepository.findByMemberAndCode(member, dto.getCode())
                .orElseThrow(() -> new IllegalArgumentException("인증번호가 잘못되었습니다."));

        // 만료 체크
        if (token.getExpiryAt().before(new Date())) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }

        // 사용 완료 체크
        if ("Y".equals(token.getUsed())) {
            throw new IllegalArgumentException("이미 사용된 인증번호입니다.");
        }

        // 시도 횟수 체크
        token.setAttempts(token.getAttempts() + 1);
        if (token.getAttempts() >= 5) {
            token.setUsed("Y"); // 강제 만료
            emailCertifyRepository.save(token);
            throw new IllegalArgumentException("5회 이상 틀렸습니다. 인증번호를 다시 발급받으세요.");
        }

        // 코드 검증
        if (!token.getCode().equals(dto.getCode())) {
            emailCertifyRepository.save(token); // 시도 횟수 증가 저장
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 성공
        return true;
    }

    /** 비밀번호 재설정 */
    @Transactional
    public void resetPassword(ResetPasswordRequest dto) {
        Member member = memberRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        EmailCertify token = emailCertifyRepository.findByMemberAndCode(member, dto.getCode())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 정보입니다."));

        //사용체크
        if ("Y".equals(token.getUsed())) {
            throw new IllegalArgumentException("이미 사용된 인증번호입니다.");
        }
        //만료체크
        if (token.getExpiryAt().before(new Date())) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }

        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        memberRepository.save(member);

        token.setUsed("Y");
        emailCertifyRepository.save(token);
    }

    private String generate6DigitCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }


}
