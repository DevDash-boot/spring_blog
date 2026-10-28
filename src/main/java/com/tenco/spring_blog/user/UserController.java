package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseResponse;
import com.tenco.spring_blog.purchase.PurchaseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리
public class UserController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;
    private final PurchaseService purchaseService;

    // GET http://localhost:8080/user/point/charge (포인트 충전 화면)
    @GetMapping("/user/point/charge")
    public String chargePointForm(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null) {
            return "redirect:/login";
        }
        User user = userService.findById(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/charge-point";
    }

    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getAuthorizeUrl();
    }

    // 리다이랙트로 들어와서 카카오 서버가 코드값을 동적으로 뽑아주는 URL
    @GetMapping("/oauth/kakao")
    public String kakaoCallback(@RequestParam(name = "code", required = false) String code, HttpSession session) {
        SessionUserDto sessionUserDto = kakaoLoginService.login(code);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);
        return "redirect:/";
    }

    // 회원가입 화면으로 이동
    // GET - http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원가입
    @PostMapping("/join")
    public String join(UserRequest.joinDto joinDto, HttpSession session) {
        joinDto.validate();
        // 이메일 인증 완료된 값 꺼내오기(세션에 저장된 상태)
        String verifiedEmail = (String) session.getAttribute(Define.VERIFIED_EMAIL);
        userService.join(joinDto, verifiedEmail);
        // 가입 완료 후 임시 저장해둔 세션 이메일 정보 제거
        session.removeAttribute(Define.VERIFIED_EMAIL);
        return "redirect:/login";
    }

    // 로그인 화면으로 이동
    // GET - http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(UserRequest.loginDto loginDto, HttpSession session) {
        loginDto.validate();
        SessionUserDto sessionUserDto = userService.login(loginDto);
        session.setAttribute(Define.SESSION_USER, sessionUserDto);
        return "redirect:/";
    }

    // 회원정보 수정 화면 요청
    // GET - http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(HttpSession session, Model model) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.updateFormDto userUpdateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", userUpdateFormDto);
        return "user/update-form";
    }

    @PostMapping("/user/update")
    public String update(HttpSession session, UserRequest.updateDto updateDto) {
        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updatesessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);
        session.setAttribute(Define.SESSION_USER, updatesessionUserDto);
        return "redirect:/user/detail";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // GET http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/detail";
    }

    // 프로필 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/profile";
    }

    @GetMapping("/user/purchase/list")
    public String purchaseList(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        List<PurchaseResponse.ListDto> purchaseList= purchaseService.findAllByUserId(sessionUserDto.getId());
        model.addAttribute("purchaseList", purchaseList);
        return "user/purchase-list";
    }
}
