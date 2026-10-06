package com.tenco.spring_blog.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(UserPersistRepository.class)
@DataJpaTest // JPA 테스트에 필요한 환경을 자동으로 구성합니다.
public class UserPersistRepositoryTest {

    @Autowired // DI
    private UserPersistRepository userPersistRepository;

    @Test
    public void findByUsernameAndPassword_로그인_성공_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistRepository.save(user);

        // when : 사용자명과 비밀번호로 조회
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                "testUser",
                "3456"
        );

        // then : 조회된 사용자 검증
        Assertions.assertThat(loginUser).isNotNull();
        Assertions.assertThat(loginUser.getId()).isNotNull();
        Assertions.assertThat(loginUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(loginUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(loginUser.getEmail()).isEqualTo("test@email.com");

        // 저장한 객체와 조회한 객체가 같은 엔티티인지 확인
        Assertions.assertThat(loginUser).isSameAs(user);
    }

    @Test
    public void findByUsernameAndPassword_비밀번호_불일치_테스트() {

        // given : 회원가입된 사용자 정보
        User user = User.builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        userPersistRepository.save(user);

        // when : 잘못된 비밀번호로 로그인 시도
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                "testUser",
                "9999"
        );

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }

    @Test
    public void findByUsernameAndPassword_존재하지않는_사용자_테스트() {

        // given : DB에 존재하지 않는 사용자 정보
        String username = "xxxxx";
        String password = "3456";

        // when : 존재하지 않는 사용자로 로그인 시도
        User loginUser = userPersistRepository.findByUsernameAndPassword(
                username,
                password
        );

        // then : 로그인 실패 -> null
        Assertions.assertThat(loginUser).isNull();
    }

    // 단위_테스트할 메서드를 설계
    @Test
    public void save_회원가입_테스트() {
        // given : 회원 가입시 사용자 정보
        User user = User.
                builder()
                .username("testUser")
                .password("3456")
                .email("test@email.com")
                .build();

        // 저장 전 상태 확인 : ID 는 저장던에 null 이어야 한다.
        Assertions.assertThat(user.getId()).isNull();
        System.out.println("저장 전 User : " + user);

        // when : 회원 가입 실행 (영속화)
        User savedUser = userPersistRepository.save(user);

        // then : 저장된 결과를 검증
        // 1. 자동 생성된 ID 값 확인
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedUser.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedUser.getUsername()).isEqualTo("testUser");
        Assertions.assertThat(savedUser.getPassword()).isEqualTo("3456");
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("test@email.com");

        // 3. 원본 객체외 반환된 객체가 동일한 참조인지 확인
        // 영속성 컨텍스트는 같은 엔티티에 대해 같은 인스턴를 보장한다.
        Assertions.assertThat(user).isSameAs(savedUser); // true, false

    }

    @Test
    public void findByUsername_존재하지않는_사용자_테스트() {

        // given : 존재하지 않는 사용자명
        String username = "xxxxx";
        // when
        User notFoundUser = userPersistRepository.findByUsername(username);
        // then : null 반환 확인
        Assertions.assertThat(notFoundUser).isNull();
    }

}
