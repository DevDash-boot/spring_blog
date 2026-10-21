package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.UnauthorizedException;
import com.tenco.spring_blog._core.util.Define;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC (제어의 역전) 싱글톤 패턴으로 관리
public class UserController {

    private final UserService userService;

    // application.yaml의 변수를 가지고 올 때 사용
    // lombok의 value가 아니라 import org.springframework.beans.factory.annotation.Value;
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @GetMapping("/kakao-login")
    public String kakaoLogin() {
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";
        return "redirect:" + url;
    }

    // 흐름 : 1. 인가 코드 받기 -> 2. 액세스 토큰 요청 -> 3. 사용자 정보 요청 -> 4. 가입 또는 로그인
    @GetMapping("/oauth/kakao")
    // 1. 인가 코드 받기
    public String kakaoCallback(@RequestParam(name= "code", required = false)String code, HttpSession session){
        if(code == null){
            throw new UnauthorizedException("카카오 로그인이 취소되었습니다.");
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserResponse.OauthToken> tokenResponse;
        ResponseEntity<UserResponse.KakaoUserInfo> profileResponse;
        try {
            // 2. 받은 인가 코드를 활용해 토큰 요청
            HttpHeaders tokenHeaders = new HttpHeaders();
            // APPLICATION_FORM_URLENCODED = Content-Type: application/x-www-form-urlencoded;charset=utf-8
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("client_id", kakaoClientId);
            tokenParams.add("redirect_uri", kakaoRedirectUri);
            tokenParams.add("code", code);
            tokenParams.add("client_secret", kakaoClientSecret);    // 클라이언트 시크릿 기능이 활성화 되면 필수

            // HTTP 요청 메세지 : 바디 + 헤더
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity(tokenParams, tokenHeaders);

            // exchange(주소, method, HTTP 메세지, 응답 타입)
            tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    UserResponse.OauthToken.class
            );

            // 액세스 토큰으로 사용자 정보 요청(카카오 서버)
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());
            HttpEntity profileRequest = new HttpEntity(profileHeaders);
            profileResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    profileRequest,
                    UserResponse.KakaoUserInfo.class
            );

            // 내부 로직 - 최초 소셜로그인 사용자라면 자동 회원가입(필요 정보가 더 있다면 추가 기입)
            // 이미 회원 가입된 사용자라면 로그인 처리 진행
            UserResponse.KakaoUserInfo kakaoUserInfo = profileResponse.getBody();
            SessionUserDto sessionUserDto = userService.kakaoLogin(kakaoUserInfo);
            session.setAttribute(Define.SESSION_USER, sessionUserDto);

            return "redirect:/";
        } catch (HttpClientErrorException e) {
            // 카카오가 4xx을 응답한 경우
            log.error("카카오 API 오류: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException("카카오 로그인에 실패했습니다.");
        }
    }

    // 회원가입 화면으로 이동
    // GET - http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // 회원가입
    @PostMapping("/join")
    public String join(UserRequest.joinDto joinDto) {
        joinDto.validate();
        userService.join(joinDto);
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
}
