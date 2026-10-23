package com.tenco.spring_blog.sms;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;

public class SmsRequest {
    // 휴대폰 인증 요청 DTO
    @Data
    public static class smsCheckDto {
        private String telNumber;   // 휴대폰 번호(1번째 요청 시 사용)
        private String code;    // 인증 번호(2번째 요청 시 사용)

        public void validate() {
            if (telNumber == null || telNumber.trim().isEmpty()) {
                throw new Exception400("휴대폰 번호를 입력해주세요");
            }
        }

        // 인증 번호 확인. API에서 추가로 호출할 예정
        public void validateCode() {
            validate();
            if (code == null || code.trim().isEmpty()) {
                throw new Exception400("인증 번호를 입력해주세요.");
            }
        }
    }

}
