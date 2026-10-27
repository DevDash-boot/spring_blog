package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
 서비스 레이어
  - 비즈니스 로직을 처리하는 계층
  - Controller 와 Repository 사이의 중간 계층 담당
  - 트랜잭션 관리

 계층 구조 : 3 - Tier 아키텍처 (C -> S -> R -> DB)
 */
@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    /*
     * 회원가입 처리
     * @param joinDto : 회원가입 정보
     * @return : 저장된 사용자 정보
     */
    @Transactional
    public UserResponse.JoinDto join(UserRequest.joinDto joinDto, String verifiedEmail) {
        log.info("회원가입 서비스 처리 시작 - 사용자명 : {}", joinDto.getUsername());

        // 이메일 인증 여부 확인
        // 인증하지 않았거나 인증한 이메일과 가입 폼의 이메일이 다르면 거부
        if (verifiedEmail == null || !verifiedEmail.equals(joinDto.getEmail())) {
            throw new Exception400("이메일 인증을 먼저 완료해주세요.");
        }

        // 중복 체크 : 이미 있으면 null이 아님
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원 가입 실패 - 중복된 사용자명 : {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명입니다.");
                });
        // 이메일 중복 검사
        checkEmailAvailable(joinDto.getEmail());

        // 프로필 이미지 저장(새롭게 생긴 파일 이름이 리턴)
        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());

        // 비밀번호 해싱 처리
        String encodePassword = passwordEncoder.encode(joinDto.getPassword());

        // 프로필 파일 이름과 회원 정보 저장
        User user = joinDto.toEntity(profileImage, encodePassword);
        user.addRole(Role.USER);    // Role 기본값 할당
        User savedUser = userJpaRepository.save(user);
        log.info("회원가입 서비스 처리 완료 - 사용자명 : {}, ID : {}", joinDto.getUsername(), savedUser.getId());
        return new UserResponse.JoinDto(savedUser);
    }

    /*
     * 로그인 처리
     * @param loginDto : 로그인 정보
     * @return : 로그인된 사용자 정보
     */
    public SessionUserDto login(UserRequest.loginDto loginDto) {
        log.info("로그인 서비스 처리 시작 - 사용자명 : {}", loginDto.getUsername());
        // 사용자명으로만 조회(역할 포함)
        User user = userJpaRepository.findByUsernameWithRoles(loginDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명 : {}, 원인 : 사용자명 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");

                });
        // 비밀번호 비교 : matches(입력한 평문, DB에 저장된 해시)
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 : {}, 원인 : 비밀번호 불일치", loginDto.getPassword());
            throw new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("로그인 성공 : {}", loginDto.getUsername());
        return new SessionUserDto(user);
    }

    // 회원 정보 수정 화면 : 화면에 필요한 값만 담는 DTO 반환
    public UserResponse.updateFormDto findUpdateForm(Long userId) {
        User user = findById(userId);
        return new UserResponse.updateFormDto(user);
    }

    // 회원 정보 수정 처리(Dirty Checking 활용)
    // 세션 동기화용 DTO 반환
    @Transactional
    public SessionUserDto updateById(Long id, UserRequest.updateDto updateDto) {
        User user = findById(id);   // 정보 조회

        if (updateDto.hasPassword()) {
            // 비밀번호 해싱 처리
            user.update(passwordEncoder.encode(updateDto.getPassword()));   // 비밀번호
        }

        if (updateDto.hasProfileImage()) {
            // 새 이미지를 골랐을 때만 교체 (null 또는 파일 데이터
            String newImage = FileUtil.saveImage(updateDto.getProfileImage());
            String oldImage = user.getProfileImage();
            user.updateProfileImage(newImage);  // DB에는 새 파일 이름 변경 감지
            FileUtil.deleteFile(oldImage);  // 기존 파일은 서버 측 폴더에서 삭제처리
        }
        return new SessionUserDto(user);    // 세션 동기화용 DTO 변환
    }

    // 사용자 정보 조회
    // 서비스 안에서만 쓰는 조회용 메서드(캡슐화)
    public User findById(Long id) {
        log.info("사용자 정보 조회 - ID : {}", id);
        return userJpaRepository.findById(id).orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    public UserResponse.DetailDto findDetail(Long userId) {
        User user = findById(userId);
        return new UserResponse.DetailDto(user);
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findById(userId);
        String oldImage = user.getProfileImage();
        user.updateProfileImage(null);  // DB에 파일 이름 비우기
        FileUtil.deleteFile(oldImage);  // 폴더에 저장된 파일 삭제
    }

    // 카카오 로그인 : 처음이면 자동 회원 가입, 기존 회원이면 로그인
    @Transactional
    public SessionUserDto kakaoLogin(UserResponse.KakaoUserInfo kakaoUserInfo) {
        // 1. 카카오 회원 번호로 우리 서비스의 사용자명 만들기
        String username = "kakao_" + kakaoUserInfo.getId();

        // 2. 기존 회원인지 확인. 없으면 자동 회원 가입
        User user = userJpaRepository.findByUsername(username)
                .orElseGet(() -> joinKakaoUser(username, kakaoUserInfo));

        // 같은 이름의 일반 회원이 있다면 카카오 사용자로 로그인 시키지 않음
        if (user.getProvider() != OAuthProvider.KAKAO) {
            throw new Exception400("이미 사용중인 사용자명입니다.");
        }

        // 트랜잭션 안에서 SessionUserDto 생성
        return new SessionUserDto(user);
    }

    private User joinKakaoUser(String username, UserResponse.KakaoUserInfo kakaoUserInfo) {
        User newUser = User.builder()
                .username(username)
                // 카카오 사용자는 비밀번호로 로그인하지 않으므로 아무도 알 수 없는 값을 넣음
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(username + "@kakao.com")
                .profileImage(kakaoUserInfo.getProperties().getProfileImage())
                .provider(OAuthProvider.KAKAO)
                .build();
        newUser.addRole(Role.USER);
        return userJpaRepository.save(newUser);
    }

    // 이메일 사용 가능 여부(인증 번호 발송 전)
    public void checkEmailAvailable(String email) {
        userJpaRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new Exception400("이미 등록된 이메일입니다.");
                });
    }

    @Transactional
    public int chargePoint(Long userId, Integer amount) {
        User user = findById(userId);
        user.chargePoint(amount);   // 더티 체킹으로 UPDATE됨
        log.info("포인트 충전 완료. 사용자 ID : {}, 충전 포인트 : {}, 보유 포인트 : {}", userId, amount, user.getPoint());
        return user.getPoint();
    }
}
