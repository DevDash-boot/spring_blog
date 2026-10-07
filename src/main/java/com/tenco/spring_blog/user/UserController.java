package com.tenco.spring_blog.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리
public class UserController {

    private final UserPersistRepository userPersistRepository;

    // 회원가입 화면으로 이동
    // GET - http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원가입
    @PostMapping("/join")
    public String join(UserRequest.joinDto joinDto, Model model) {
        log.info("=== 회원가입 요청 ===");
        log.info("사용자명 {}", joinDto.getUsername());
        log.info("비밀번호 {}", joinDto.getPassword());
        log.info("이메일 {}", joinDto.getEmail());

        try {
            // 1. 유효성 검사
            joinDto.validate();

            // 2-2. 사용자명 중복 체크
            User existingUser = userPersistRepository.findByUsername(joinDto.getUsername());
            if (existingUser != null) {
                throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
            }

            // 3. DTO를 Entity로 변환
            User user = joinDto.toEntity();

            // 4. DB에 회원 정보 저장
            User userEntity = userPersistRepository.save(user);

            // 회원가입 성공 시 로그인 화면으로 이동
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/join-form";
        }
    }

    // 로그인 화면으로 이동
    // GET - http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(UserRequest.loginDto loginDto, HttpSession session, Model model) {
        log.info("=== 로그인 요청 ===");
        log.info("사용자명 : {}", loginDto.getUsername());
        try {
            // 1. 입력 데이터 검증
            loginDto.validate();

            // 2. 사용자 명과 비밀번호로 사용자 조회
            User sessionUser = userPersistRepository.findByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword());

            // 3. 로그인 실패 처리
            if(sessionUser == null){
                // 로그인 실패 : 일치하는 사용자 없음
                throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다.");
            }

            // mustache가 세션 값을 기본으로 읽지 않는 설정이 되어 있다.
            // 머스태치 파일에서 세션 메모리에 접근할 수 있도록 설정 추가(application.yaml)

            // 4. 로그인 성공 : 세션에 사용자 정보 저장
            sessionUser.setPassword(null);
            session.setAttribute("sessionUser", sessionUser);
            log.info("로그인한 사용자 : {} ", sessionUser.getUsername());

            // 5. 메인 페이지로 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            // 로그인 실패시 에러 메시지와 함께 로그인 폼으로 돌려보내기
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login-form";
        }
    }

    // 회원정보 수정 화면 요청
    // GET - http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(HttpSession session, Model model) {
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userPersistRepository.findById(sessionUser.getId());
        model.addAttribute("user", user);
        return "user/update-form";
    }

    @PostMapping("/user/update")
    public String update(HttpSession session, Model model, UserRequest.updateDto updateDto) {
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        // 2. 권한 검사
        // 다른 사람의 정보는 처음부터 수정할 수 없음(대상이 실제로 있는지만 확인)
        User userEntity = userPersistRepository.findById(sessionUser.getId());

        try {
            if(userEntity == null){
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            }

            // 3. 유효성 검사
            updateDto.validate();

            // 4. 객체 상태값 변경
            User updateUser = userPersistRepository.updateById(sessionUser.getId(), updateDto);

            // 5. 세션 동기화
            updateUser.setPassword(null);
            session.setAttribute("sessionUser", updateUser);

            // 6. 성공 후 메인 페이지로 리다이렉트
            return "redirect:/";
        } catch (Exception e) {
            // 7. 예외 발생
            model.addAttribute("user", userEntity);
            model.addAttribute("errorMessage", e.getMessage());
            return "user/update-form";
        }
    }

    // GET - http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        log.info("=== 로그아웃 요청 ===");
        // 세션 무효화 처리
        session.invalidate();
        log.info("로그아웃 완료");

        return "redirect:/";
    }

}
