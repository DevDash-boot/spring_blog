package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /*
     * 회원가입 처리
     * @param joinDto : 회원가입 정보
     * @return : 저장된 사용자 정보
     */
    @Transactional
    public UserResponse.JoinDto join(UserRequest.joinDto joinDto) {
        log.info("회원가입 서비스 처리 시작 - 사용자명 : {}", joinDto.getUsername());
        // 중복 체크 : 이미 있으면 null이 아님
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    log.warn("회원 가입 실패 - 중복된 사용자명 : {}", joinDto.getUsername());
                    throw new Exception400("이미 존재하는 사용자명입니다.");
                });
        // 이메일 중복 검사
        userJpaRepository.findByEmail(joinDto.getEmail())
                .ifPresent(user -> {
                    log.warn("회원 가입 실패 - 중복된 이메일 : {}", joinDto.getEmail());
                    throw new Exception400("이미 존재하는 이메일입니다.");
                });

        // 프로필 이미지 저장(새롭게 생긴 파일 이름이 리턴)
        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());

        User user = joinDto.toEntity(profileImage);
        user.addRole(Role.USER);    // Role 기본값 할당

        // 프로필 파일 이름과 회원 정보 저장
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
        User user = userJpaRepository.findByUsernameAndPassword(loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명 : {}, 원인 : 인증정보 불일치", loginDto.getPassword());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });
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
            user.update(updateDto.getPassword());   // 비밀번호
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
    private User findById(Long id) {
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
}
