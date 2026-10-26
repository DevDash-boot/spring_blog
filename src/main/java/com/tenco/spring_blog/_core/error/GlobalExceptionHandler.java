package com.tenco.spring_blog._core.error;

import com.tenco.spring_blog._core.util.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 처리
@Slf4j
@ControllerAdvice      // IoC
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception400.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        log.warn("=== 400 Bad  Request 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("에러 메세지 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        addSessionUserDto(request, model);  // 추가(헤더에 sessionUserDto 담기)
        model.addAttribute("errorMessage", e.getMessage());

        return "err/400";
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(Exception401.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex401(Exception401 e, HttpServletRequest request, RedirectAttributes rttr) {
        log.warn("=== 401 Unauthorized 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("인증 오류 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        rttr.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(UnauthorizedException.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    @ResponseBody
    public String exUnauthorized(UnauthorizedException e, HttpServletRequest request) {
        log.warn("=== 401(Data 반환) Unauthorized 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("인증 오류 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\'"); // 따옴표 깨짐 방지
        return """
                <script>
                    alert('%s');
                    location.href = '/login'
                </script>    
                """.formatted(msg);
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
    public String ex403(Exception403 e, HttpServletRequest request, Model model) {
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception404.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        log.warn("=== 404 Not Found 에러 발생 ===");
        log.info("요청 URL : {} ", request.getRequestURL());
        log.info("에러 메세지 : {} ", e.getMessage());
        log.info("예외 클래스 : {} ", e.getClass().getSimpleName());

        addSessionUserDto(request, model);
        model.addAttribute("errorMessage", e.getMessage());

        return "err/404";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception500.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        log.error("=== 500 Internal Server Error 에러 발생 ===");
        log.error("요청 URL : {} ", request.getRequestURL());
        log.error("서버 오류 : {} ", e.getMessage());
        log.error("스택 트레이스 : ", e); // 전체 스택 트레이스 포함

        addSessionUserDto(request, model);
        model.addAttribute("errorMessage", "네트워크 일시 장애");

        return "err/500";
    }

    // 데이터베이스 제약 조건 위반(외래키, unique 중복)
    @ResponseStatus(HttpStatus.CONFLICT)    // 409 : 현재 데이터 상태와 충돌하는 지
    @ExceptionHandler(DataIntegrityViolationException.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String exDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request, Model model) {
        log.error("=== 데이터베이스 제약조건 위반 ===");
        log.error("요청 URL : {} ", request.getRequestURL());
        log.error("원인 : {}", e.getMostSpecificCause().getMessage());  // 상세 원인(SQL, 제약조건 이름)은 개발자용 로그에만 남김


        addSessionUserDto(request, model);
        model.addAttribute("errorMessage", "시스템 오류 발생. 관리자에게 문의해주세요.");

        return "err/500";
    }

    // 기타 에러 처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class) // 특정 예외 타입이 발생했을 때 실행될 메서드로 지정
    public String exRun(RuntimeException e, HttpServletRequest request, Model model) {
        log.error("=== 예상치 못한 에러 발생 ===");
        log.error("요청 URL : {} ", request.getRequestURL());
        log.error("에러 타입 : {} ", e.getClass().getSimpleName());
        log.error("에러 메세지 : {} ", e.getMessage());
        log.error("스택 트레이스 : ", e); // 전체 스택 트레이스 포함

        model.addAttribute("errorMessage", "시스템 오류 발생. 관리자에게 문의해주세요.");

        return "err/500";
    }

    // 오류 화면에서도 헤더의 로그인 메뉴가 보이도록 sessionUserDto를 Model에 추가
    private void addSessionUserDto(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(Define.SESSION_USER) != null) {
            model.addAttribute(Define.SESSION_USER, session.getAttribute(Define.SESSION_USER));
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(NotEnoughPointException.class)
    public String exNot(NotEnoughPointException e, HttpServletRequest request, Model model) {
        log.warn("=== 포인트 부족 에러 발생 ===");
        log.warn("요청 URL : {} ", request.getRequestURL());
        log.warn("권한 오류 : {} ", e.getMessage());
        log.warn("예외 클래스 : {} ", e.getClass().getSimpleName());

        String msg = e.getMessage().replace("'", "\\");    // 따옴표 깨짐 방지

        return """
                <script>
                    alert('%s');
                    location.href = '/user/detail';
                </script>
                """.formatted(msg);
    }
}
