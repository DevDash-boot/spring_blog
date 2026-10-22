package com.tenco.spring_blog._core.util;

import java.security.SecureRandom;

public class MailUtil {
    private static final SecureRandom RANDOM = new SecureRandom();  // 시큐리티 - 예측하기 어려운 난수 생성기

    // 6자리의 인증 번호 생성
    public static String generateCode() {
        int code = RANDOM.nextInt(1000000); // 000000 ~ 999999
        return String.format("%06d", code);
    }
}
