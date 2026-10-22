package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    // 메일 발송. 인증 번호 확인

    // 의존성 설정. application.yaml의 spring.mail 설정으로 스프링 부트가 만들어준 빈 객체
    private final JavaMailSender javaMailSender;

    // record 문법 활용 : JDK 16버전 부터 사용.
    // 아래 코드가 클래스를 설계해 준다.
    // 필드 + getter + equals + hashCode + toString을 가진 것을 자바가 대신 만들어준다.(setter 없음)
    // Serializable : 바이트 단위로 직렬화
    private record EmailCode(String code, LocalDateTime expiresAt) implements Serializable {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    // 1. 인증 번호 발송
    public void sendVerificationCode(String email, HttpSession session) {
        // 1-1. 6자리 인증 번호 생성
        String code = MailUtil.generateCode();
        // 1-2. 메일 작성 (MimeMessage : HTML 본문을 만들 수 있고, 첨부 파일 등을 담을 수 있는 메일 형식)
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            // false : 첨부 파일 없음. "UTF-8" : 한글 깨짐 방지
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);    // 받는 사람 이메일 주소
            helper.setSubject("[스프링 블로그] 회원가입 인증 번호");       // 제목
            helper.setText("<h3>인증 번호는 " + code + " 입니다.</h3>" +
                    "<p>" + Define.CODE_EXPIRE_MINUTES + "분 안에 입력해주세요.</p>", true);  // 본문
        } catch (Exception e) {
            throw new Exception500("메일 작성 중 오류가 발생했습니다.");
        }
        // 1-3. 인증 번호 발송
        javaMailSender.send(message);

        session.setAttribute(Define.EMAIL_CODE_PREFIX + email,
                new EmailCode(code, LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES)));
        log.info("인증 번호 발송 완료 - 이메일 : {}", email);   // 인증 번호 자체는 로그에 남기지 않는다.
    }

    // 2. 인증 번호 확인 : 일치하면 true
    public boolean verifyCode(String email, String code, HttpSession session) {
        String key = Define.EMAIL_CODE_PREFIX + email;
        EmailCode savedEmailCode = (EmailCode) session.getAttribute(key);
        if (savedEmailCode == null) {
            throw new Exception400("인증 번호를 먼저 요청해주세요.");
        }
        if (savedEmailCode.isExpired()) {
            session.removeAttribute(key);   // 만료된 인증 번호 삭제
            throw new Exception400("인증 번호가 만료되었습니다. 다시 요청해 주세요.");
        }
        if(!savedEmailCode.code().equals(code)){
            throw new Exception400("인증 번호가 일치하지 않습니다.");
        }

        // 성공 시 사용한 인증 번호는 삭제(한 번만 사용할 수 있게)
        session.removeAttribute(key);
        // 인증을 완료한 다음 이메일을 세션에 기록해야한다.(회원가입 시 서버가 확인)
        // 인증된 이메일이 아닌 경우 변조해서 던질 수 있다.
        session.setAttribute(Define.VERIFIED_EMAIL, email);
        return true;
    }
}
