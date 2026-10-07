                                                      package com.tenco.spring_blog._core.error;

// 500 Internal Server Error 상황에 사용할 사용자 정의 예외 클래스(커스텀)
// RuntimeException을 상속하여 unchecked 예외로 만듦
public class Exception500 extends RuntimeException{
    // 예외 메시지를 받을 수 있도록 String 파라미터 설계
    public Exception500(String msg) {
        super(msg); // 부모 클래스의 메시지 설정
    }

    public static void main(String[] args) {
        throw new Exception500("필수 입력 항목이 누락되었습니다.");
    }
}
