package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SmsApiController {
    private final UserService userService;
    private final SmsService smsService;

    @PostMapping("/api/sms/send")
    public ResponseEntity<?> sendCode(@RequestBody SmsRequest.smsCheckDto smsCheckDto, HttpSession session) {
        smsCheckDto.validate();
        smsService.sendVerificationCode(smsCheckDto.getTelNumber(), session);
        return ResponseEntity.ok(Map.of("message", "인증 번호 발송 되었습니다."));
    }

    @PostMapping("api/sms/verify")
    public ResponseEntity<?> verifyCode(@RequestBody SmsRequest.smsCheckDto smsCheckDto, HttpSession session) {
        smsCheckDto.validateCode();
        boolean verified = smsService.verifyCode(smsCheckDto.getTelNumber(), smsCheckDto.getCode(), session);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
        }
        SessionUserDto sessionUser = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUser == null) {
            throw new Exception400("로그인이 필요합니다.");
        }
        userService.certifyPhone(sessionUser.getId());
        return ResponseEntity.ok().body(Map.of("message", "휴대폰 인증되었습니다."));
    }

    // 이 컨트롤러에서 발생한 예외는 json으로 별도 응답 처리
    @ExceptionHandler(Exception400.class)
    public ResponseEntity<Map<String, String>> handleEx400(Exception400 e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleEx500(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "인증 문자 발송 중 오류가 발생했습니다."));
    }
}
