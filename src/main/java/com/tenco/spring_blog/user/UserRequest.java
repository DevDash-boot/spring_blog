package com.tenco.spring_blog.user;

import com.tenco.spring_blog._core.error.Exception400;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {

    // 포인트 추천 요청 DTO(테스트용 충전 API, 실제 서비스는 결제 연동으로 대체)
    @Data
    public static class PointChargeDto {
        private Integer amount; // 충전할 포인트

        public void validate() {
            if (amount == null || amount <= 0) {
                throw new Exception400("충전할 포인트는 0보다 커야합니다.");
            }
            if (amount > 100000) {
                throw new Exception400("한 번에 10만 포인트까지만 충전할 수 있습니다.");
            }
        }
    }

    // 이메일 인증 요청 DTO(발송, 확인 API와 함께 사용)
    @Data
    public static class emailCheckDto {
        private String email;   // 이메일 주소(1번째 요청 시 사용)
        private String code;    // 인증 번호(2번째 요청 시 사용)

        public void validate() {
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일을 입력해주세요");
            }
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        // 인증 번호 확인. API에서 추가로 호출할 예정
        public void validateCode() {
            validate();
            if (code == null || code.trim().isEmpty()) {
                throw new Exception400("인증 번호를 입력해주세요.");
            }
        }
    }

    // 회원가입 용 DTO 설계
    @Data
    public static class joinDto {
        private String username;
        private String password;
        private String email;
        private MultipartFile profileImage; // 프로필 이미지(선택 사항)

        // 회원가입 시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수입니다.");
            }
            // 간단하게 이메일 형식 검증
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        // DTO 에서 User 엔티티로 변환하는 메서드
        // 계층 간 데이터 변환을 명확하게 분리하는 것이 좋다.
        public User toEntity(String profileImageFilename, String encodePassword) {
            return User.builder()
                    .username(username)
                    .password(encodePassword)
                    .email(email)
                    .profileImage(profileImageFilename)
                    .build();
        }
    }

    // 로그인
    @Data
    public static class loginDto {
        private String username;
        private String password;

        // 로그인 시 데이터 검증 메서드
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호는 필수입니다.");
            }
        }
    }

    // 회원정보 수정용 DTO
    @Data
    public static class updateDto {
        private String password;
        private MultipartFile profileImage; // 새 프로필 이미지(선택 사항)

        // 비밀번호를 입력했는지 확인(null, 공백 입력 -> 입력 안함)
        public boolean hasPassword() {
            return password != null && !password.isBlank(); // 비밀번호 입력시 true 반환
        }

        // 새 프로필 사진을 골랐는지
        public boolean hasProfileImage() {
            return profileImage != null && !profileImage.isEmpty(); // 새 파일 선택 시 true 반환
        }

        public void validate() {
            if (!hasPassword() && !hasProfileImage()) {   // 둘 다 입력 안함
                throw new Exception400("비밀번호 또는 프로필 이미지를 변경해주세요.");
            }

            // 필요하다면 비밀번호 길이 제한, 특수문자 포함 여부 활용 가능
            if (hasPassword() && password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이어야 합니다.");
            }
        }
    }
}
