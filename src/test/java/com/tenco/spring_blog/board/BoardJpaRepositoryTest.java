package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // 테스트용 작성자를 직접 저장한다.
    private User 작성자_생성(String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .password("1234")
                .build(); // 확인 필요: User 빌더에 username, email, password 필드가 있다고 가정

        entityManager.persist(user);
        entityManager.flush();

        return user;
    }

    // 테스트용 게시글을 직접 저장한다.
    private Board 게시글_생성(User user, String title, String content) {
        Board board = Board.builder()
                .title(title)
                .content(content)
                .user(user)
                .build(); // 확인 필요: Board 빌더에 title, content, user 필드가 있다고 가정

        entityManager.persist(board);
        entityManager.flush();

        return board;
    }

    @Test
    @DisplayName("게시글 ID로 작성자와 함께 조회한다")
    void findByIdJoinUser_존재하는_게시글_테스트() {
        // given: 작성자와 게시글을 저장한다.
        User user = 작성자_생성("boarduser");
        Board savedBoard = 게시글_생성(user, "테스트 제목", "테스트 내용");

        // when: 저장한 게시글 ID로 조회한다.
        Optional<Board> result =
                boardJpaRepository.findByIdJoinUser(savedBoard.getId());

        // then: 조회 결과에 저장한 게시글이 존재한다.
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedBoard.getId());
    }

    @Test
    @DisplayName("게시글 조회 시 작성자 정보가 함께 로딩된다")
    void findByIdJoinUser_작성자_함께로딩_테스트() {
        // given: 작성자와 게시글을 저장하고 영속성 컨텍스트를 비운다.
        User user = 작성자_생성("fetchuser");
        Board savedBoard = 게시글_생성(user, "조회 테스트", "작성자 로딩 테스트");
        entityManager.clear();

        // when: JOIN FETCH 쿼리로 게시글을 조회한다.
        Board foundBoard = boardJpaRepository
                .findByIdJoinUser(savedBoard.getId())
                .orElseThrow();

        // then: 게시글의 user 연관 엔티티가 로딩된 상태인지 확인한다.
        PersistenceUnitUtil persistenceUnitUtil =
                entityManagerFactory.getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(foundBoard, "user")).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 빈 Optional을 반환한다")
    void findByIdJoinUser_존재하지않는_게시글_테스트() {
        // given: 실제로 사용하지 않을 게시글 ID를 준비한다.
        Long boardId = Long.MAX_VALUE;

        // when: 해당 ID로 게시글을 조회한다.
        Optional<Board> result = boardJpaRepository.findByIdJoinUser(boardId);

        // then: 조회 결과가 비어 있다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("게시글 목록의 페이징과 정렬을 확인한다")
    void findAllJoinUser_페이징_테스트() {
        // given: 같은 작성자가 작성한 게시글 5개를 저장한다.
        User user = 작성자_생성("pageuser");
        for (int i = 1; i <= 5; i++) {
            게시글_생성(user, "제목 " + i, "내용 " + i);
        }
        entityManager.clear();

        // when: ID 내림차순으로 한 페이지에 2개씩 조회한다.
        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.DESC, "id")
        );
        Page<Board> result = boardJpaRepository.findAllJoinUser(pageable);

        // then: 페이지 크기, 전체 데이터 수, 전체 페이지 수, 정렬 순서를 확인한다.
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getContent().get(0).getId())
                .isGreaterThan(result.getContent().get(1).getId());
    }

    @Test
    @DisplayName("페이징 조회 시 작성자 정보가 함께 로딩된다")
    void findAllJoinUser_작성자_함께로딩_테스트() {
        // given: 작성자와 게시글을 저장하고 영속성 컨텍스트를 비운다.
        User user = 작성자_생성("pagefetchuser");
        게시글_생성(user, "페이징 조회", "작성자 로딩 확인");
        entityManager.clear();

        // when: 작성자 JOIN FETCH가 적용된 페이징 쿼리를 실행한다.
        Page<Board> result = boardJpaRepository.findAllJoinUser(
                PageRequest.of(0, 10)
        );

        // then: 조회된 첫 게시글의 user 연관 엔티티가 로딩되어 있다.
        Board foundBoard = result.getContent().get(0);
        PersistenceUnitUtil persistenceUnitUtil =
                entityManagerFactory.getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(foundBoard, "user")).isTrue();
    }

    @Test
    @DisplayName("게시글 제목에 포함된 키워드로 검색한다")
    void findAllKeywordJoinUser_제목_검색_테스트() {
        // given: 제목에만 검색어가 포함된 게시글을 저장한다.
        User user = 작성자_생성("titleuser");
        게시글_생성(user, "스프링 검색", "일반적인 내용");
        게시글_생성(user, "자바 공부", "다른 내용");

        // when: 제목 또는 내용에서 '스프링'을 검색한다.
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "스프링"
        );

        // then: 검색어가 포함된 게시글 한 개만 조회된다.
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("스프링 검색");
    }

    @Test
    @DisplayName("게시글 내용에 포함된 키워드로 검색한다")
    void findAllKeywordJoinUser_내용_검색_테스트() {
        // given: 내용에만 검색어가 포함된 게시글을 저장한다.
        User user = 작성자_생성("contentuser");
        게시글_생성(user, "일반 제목", "JPA 검색 연습");
        게시글_생성(user, "다른 제목", "자바 공부");

        // when: 제목 또는 내용에서 'JPA'를 검색한다.
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "JPA"
        );

        // then: 내용에 검색어가 포함된 게시글 한 개만 조회된다.
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getContent())
                .isEqualTo("JPA 검색 연습");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다")
    void findAllKeywordJoinUser_검색_결과_없음_테스트() {
        // given: 검색어가 포함되지 않은 게시글을 저장한다.
        User user = 작성자_생성("noresultuser");
        게시글_생성(user, "스프링", "프레임워크");
        게시글_생성(user, "자바", "프로그래밍");

        // when: 존재하지 않는 키워드로 검색한다.
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), "없는키워드"
        );

        // then: 검색 결과가 없다.
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드로 검색하면 모든 게시글을 조회한다")
    void findAllKeywordJoinUser_빈_키워드_테스트() {
        // given: 게시글 3개를 저장한다.
        User user = 작성자_생성("emptykeyworduser");
        게시글_생성(user, "스프링", "서버 개발");
        게시글_생성(user, "자바", "객체지향");
        게시글_생성(user, "JPA", "데이터베이스");

        // when: 빈 문자열로 검색한다.
        Page<Board> result = boardJpaRepository.findAllKeywordJoinUser(
                PageRequest.of(0, 10), ""
        );

        // then: 빈 문자열 검색은 모든 게시글과 일치한다.
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}