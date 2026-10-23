package com.tenco.spring_blog.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception500;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog._core.util.MailUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    // application.yaml의 변수를 가지고 올 때 사용
    @Value("${solapi.api-key}")
    private String solapiKey;
    @Value("${solapi.api-secret}")
    private String solapiSecret;
    @Value("${solapi.sender}")
    private String solapiSender;

    private record SmsCode(String code, LocalDateTime expiresAt) implements Serializable {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    // 1. 인증 번호 발송
    public void sendVerificationCode(String telNumber, HttpSession session) {
        // 6자리 인증 번호 생성
        String code = MailUtil.generateCode();
        DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance(solapiKey, solapiSecret);
        Message message = new Message();
        message.setFrom(solapiSender);
        message.setTo(telNumber);
        message.setText("[스프링 블로그] 인증 번호 " + code + " 입니다." +
                Define.CODE_EXPIRE_MINUTES + "분 안에 입력해주세요.");

        try {
            messageService.send(message);
        } catch (Exception e) {
            throw new Exception500("전화번호 작성 중 오류가 발생했습니다.");
        }

        // 인증번호와 만료 시간 저장
        session.setAttribute(Define.SMS_CODE_PREFIX + telNumber,
                new SmsCode(code, LocalDateTime.now().plusMinutes(Define.CODE_EXPIRE_MINUTES))
        );
    }

    // 2. 인증 번호 확인 : 일치하면 true
    public boolean verifyCode(String telNumber, String code, HttpSession session) {
        String key = Define.SMS_CODE_PREFIX + telNumber;
        SmsCode savedSmsCode = (SmsCode) session.getAttribute(key);
        if (savedSmsCode == null) {
            throw new Exception400("인증 번호를 먼저 요청해주세요.");
        }
        if (savedSmsCode.isExpired()) {
            session.removeAttribute(key);   // 만료된 인증 번호 삭제
            throw new Exception400("인증 번호가 만료되었습니다. 다시 요청해 주세요.");
        }
        if(!savedSmsCode.code().equals(code)){
            throw new Exception400("인증 번호가 일치하지 않습니다.");
        }

        // 성공 시 사용한 인증 번호는 삭제(한 번만 사용할 수 있게)
        session.removeAttribute(key);
        // 인증을 완료한 다음 이메일을 세션에 기록해야한다.(회원가입 시 서버가 확인)
        // 인증된 이메일이 아닌 경우 변조해서 던질 수 있다.
        session.setAttribute(Define.VERIFIED_TEL_NUMBER, telNumber);
        return true;
    }


}
