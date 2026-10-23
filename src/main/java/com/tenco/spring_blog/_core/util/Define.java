package com.tenco.spring_blog._core.util;

public class Define {
    public static final String SESSION_USER = "sessionUserDto";
    // 이메일 인증
    public static final String EMAIL_CODE_PREFIX = "emailCode_";    // emailCode_a@naver.com
    public static final String VERIFIED_EMAIL = "verified";         // 인증을 마친 이메일 주소
    public static final int CODE_EXPIRE_MINUTES = 5;    // 인증 번호 유효 시간
    public static final String SMS_CODE_PREFIX = "smsCode_";
    public static final String VERIFIED_TEL_NUMBER = "verifiedSMS";
}
