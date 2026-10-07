package com.tenco.spring_blog._core.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리
@Slf4j
@ControllerAdvice      // IoC
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception400.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex400(Exception400 e, HttpServletRequest request, Model model){
        log.warn("=== 400 Bad  Request 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("에러 메세지 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/400";
    }

    @ExceptionHandler(Exception401.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex401(Exception401 e, HttpServletRequest request, Model model){
        log.warn("=== 401 Unauthorized 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("인증 오류 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/401";
    }

//    @ExceptionHandler(Exception403.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
//    public String ex403(Exception403 e, HttpServletRequest request, Model model){
//        log.warn("=== 403 Forbidden 에러 발생 ===");
//        log.warn("요청 URL : {} ", request.getRequestURL());
//        log.warn("권한 오류 : {} ", e.getMessage());
//        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());
//
//        model.addAttribute("msg", e.getMessage());
//
//        return "err/403";
//    }

    // alert 창이 뜰 수 있게 수정
    @ResponseStatus(HttpStatus.FORBIDDEN)   // 상태코드 403 지정(없으면 200으로 나감)
    @ResponseBody
    @ExceptionHandler(Exception403.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex403(Exception403 e, HttpServletRequest request, Model model){
        log.warn("=== 403 Forbidden 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("권한 오류 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\");    // 따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    history.back();
                </script>
                """.formatted(msg);
    }

    @ExceptionHandler(Exception404.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex404(Exception404 e, HttpServletRequest request, Model model){
        log.warn("=== 404 Not Found 에러 발생 ===");
        log.info("요청 URL : {} ", request.getRequestURL());
        log.info("에러 메세지 : {} ", e.getMessage());
        log.info("예외 클래스 : {} ", e.getClass().getSimpleName());

        model.addAttribute("msg", e.getMessage());

        return "err/404";
    }

    @ExceptionHandler(Exception500.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex500(Exception500 e, HttpServletRequest request, Model model){
        log.error("=== 500 Internal Server Error 에러 발생 ===");
        log.error("요청 URL : {} ", request.getRequestURL());
        log.error("서버 오류 : {} ", e.getMessage());
        log.error("스택 트레이스 : ", e); // 전체 스택 트레이스 포함

        model.addAttribute("msg", "네트워크 일시 장애");

        return "err/500";
    }

    // 기타 에러 처리
    @ExceptionHandler(RuntimeException.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String exRun(RuntimeException e, HttpServletRequest request, Model model){
        log.error("=== 예상치 못한 에러 발생 ===");
        log.error("요청 URL : {} ", request.getRequestURL());
        log.error("에러 타입 : {} ", e.getClass().getSimpleName());
        log.error("에러 메세지 : {} ", e.getMessage());
        log.error("스택 트레이스 : ", e); // 전체 스택 트레이스 포함

        model.addAttribute("msg", "시스템 오류 발생. 관리자에게 문의해주세요.");

        return "err/500";
    }
}
