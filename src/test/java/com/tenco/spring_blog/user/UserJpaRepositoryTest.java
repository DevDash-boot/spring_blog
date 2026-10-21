package com.tenco.spring_blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // 사용자 테스트 데이터 생성
    private User 사용자_생성(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password("1234")
                .build();

        return userJpaRepository.saveAndFlush(user);
    }

    @Test
    @DisplayName("사용자명으로 역할과 함께 사용자를 조회한다")
    void findByUsernameWithRoles_일치하는_사용자_테스트() {
        // given: 조회할 사용자를 직접 저장한다.
        User savedUser = 사용자_생성("testuser", "test@example.com");

        // when: 사용자명으로 사용자를 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles("testuser");

        // then: 조회된 사용자의 ID가 저장한 사용자의 ID와 같다.
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("사용자 조회 시 roles 연관 엔티티가 함께 로딩된다")
    void findByUsernameWithRoles_역할_함께로딩_테스트() {
        // given: 사용자 한 명을 저장한다.
        사용자_생성("roleuser", "role@example.com");

        // when: 영속성 컨텍스트를 비운 후 JOIN FETCH 메서드로 조회한다.
        entityManager.flush();
        entityManager.clear();

        User foundUser = userJpaRepository
                .findByUsernameWithRoles("roleuser")
                .orElseThrow();

        // then: 조회된 User와 roles 컬렉션이 로딩된 상태인지 확인한다.
        PersistenceUnitUtil persistenceUnitUtil =
                entityManagerFactory.getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(foundUser)).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(foundUser, "roles")).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회하면 빈 Optional을 반환한다")
    void findByUsernameWithRoles_일치하지않는_사용자명_테스트() {
        // given: 조회할 사용자명을 준비한다. 해당 사용자는 저장하지 않는다.
        String username = "notfound";

        // when: 존재하지 않는 사용자명으로 조회한다.
        Optional<User> result =
                userJpaRepository.findByUsernameWithRoles(username);

        // then: 조회 결과가 비어 있다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자명으로 사용자를 조회한다")
    void findByUsername_일치하는_사용자_테스트() {
        // given
        사용자_생성("finduser", "finduser@test.com");

        // when
        Optional<User> result = userJpaRepository.findByUsername("finduser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("finduser");
    }

    @Test
    @DisplayName("등록되지 않은 사용자명으로 조회하면 빈 Optional을 반환한다")
    void findByUsername_존재하지않는_사용자_테스트() {
        // given: 등록되지 않은 사용자명을 준비한다.
        String username = "unknown";

        // when: 해당 사용자명으로 조회한다.
        Optional<User> result = userJpaRepository.findByUsername(username);

        // then: 조회 결과가 비어 있다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findByEmail_일치하는_사용자_테스트() {
        // given: 이메일을 가진 사용자를 저장한다.
        User savedUser = 사용자_생성("emailuser", "email@example.com");

        // when: 이메일로 조회한다.
        Optional<User> result =
                userJpaRepository.findByEmail("email@example.com");

        // then: 저장한 사용자와 같은 ID를 가진 사용자가 조회된다.
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("등록되지 않은 이메일로 조회하면 빈 Optional을 반환한다")
    void findByEmail_존재하지않는_이메일_테스트() {
        // given: 등록되지 않은 이메일을 준비한다.
        String email = "unknown@example.com";

        // when: 해당 이메일로 조회한다.
        Optional<User> result = userJpaRepository.findByEmail(email);

        // then: 조회 결과가 비어 있다.
        assertThat(result).isEmpty();
    }
}