package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@DataJpaTest // JPA 관련 빈만 띄우고, 각 테스트가 끝나면 롤백
public class BoardJpaRepositoryTest {

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager em; // 영속성 컨텍스트를 비우기 위해 사용

    // 게시글은 작성자(외래키)가 필요하므로 사용자를 먼저 저장
    private User saveUser() {
        return userJpaRepository.save(User.builder()
                .username("testUser")
                .password("1234")
                .email("test@email.com")
                .build());
    }

    private Board createBoard(String title, User user) {
        return Board.builder()
                .title(title)
                .content(title + " 내용")
                .user(user)
                .build();
    }

    // ===== save =====

    @Test
    public void save_게시글_저장_테스트() {
        // given : 작성자와 저장할 게시글
        User user = saveUser();
        Board board = createBoard("테스트글", user);

        // when : 저장
        Board savedBoard = boardJpaRepository.save(board);

        // then : id 와 작성일이 채워지고, 작성자가 연결됨
        Assertions.assertThat(savedBoard.getId()).isNotNull();
        Assertions.assertThat(savedBoard.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedBoard.getTitle()).isEqualTo("테스트글");
        Assertions.assertThat(savedBoard.getUser().getUsername()).isEqualTo("testUser");
    }

    // ===== findById =====

    @Test
    public void findById_존재하는_게시글_조회_테스트() {
        // given : 게시글 저장
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("테스트글", user));

        // when : id 로 조회
        Optional<Board> found = boardJpaRepository.findById(savedBoard.getId());

        // then : 조회됨
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getTitle()).isEqualTo("테스트글");
    }

    @Test
    public void findById_존재하지않는_게시글_조회_테스트() {
        // given : 존재하지 않는 id
        Long notExistId = 9999L;

        // when : 조회
        Optional<Board> found = boardJpaRepository.findById(notExistId);

        // then : 비어 있음
        Assertions.assertThat(found).isEmpty();
    }

    // ===== findAll =====

    @Test
    public void findAll_전체_조회_테스트() {
        // given : 저장 전 게시글 수 (data.sql 샘플 포함)
        int beforeSize = boardJpaRepository.findAll().size();
        User user = saveUser();
        boardJpaRepository.save(createBoard("테스트글", user));

        // when : 전체 조회
        List<Board> boardList = boardJpaRepository.findAll();

        // then : 한 개 늘어남
        Assertions.assertThat(boardList).hasSize(beforeSize + 1);
    }

    // ===== count =====

    @Test
    public void count_게시글_수_테스트() {
        // given : 저장 전 게시글 수
        long beforeCount = boardJpaRepository.count();
        User user = saveUser();
        boardJpaRepository.save(createBoard("테스트글", user));

        // when : 개수 조회
        long afterCount = boardJpaRepository.count();

        // then : 한 개 늘어남
        Assertions.assertThat(afterCount).isEqualTo(beforeCount + 1);
    }

    // ===== existsById =====

    @Test
    public void existsById_존재_여부_테스트() {
        // given : 게시글 저장
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("테스트글", user));

        // when : 존재 여부 확인
        boolean exists = boardJpaRepository.existsById(savedBoard.getId());
        boolean notExists = boardJpaRepository.existsById(9999L);

        // then
        Assertions.assertThat(exists).isTrue();
        Assertions.assertThat(notExists).isFalse();
    }

    // ===== deleteById =====

    @Test
    public void deleteById_게시글_삭제_테스트() {
        // given : 게시글 저장
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("삭제할 게시글", user));
        Long boardId = savedBoard.getId();

        // when : 삭제
        boardJpaRepository.deleteById(boardId);

        // then : 다시 조회하면 비어 있음
        Assertions.assertThat(boardJpaRepository.findById(boardId)).isEmpty();
    }

    // ===== findByIdJoinUser (상세 조회 + 작성자) =====

    @Test
    public void findByIdJoinUser_작성자_함께_조회_테스트() {
        // given : 게시글 저장 후 영속성 컨텍스트 비우기
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("테스트글", user));
        em.flush(); // INSERT 쿼리 실행
        em.clear(); // 영속성 컨텍스트를 비워서 DB 에서 새로 조회하게 함

        // when : JOIN FETCH 로 조회
        Optional<Board> found = boardJpaRepository.findByIdJoinUser(savedBoard.getId());

        // then : 작성자가 프록시가 아닌 실제 데이터로 채워져 있음 (추가 쿼리 없음)
        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(Hibernate.isInitialized(found.get().getUser())).isTrue();
        Assertions.assertThat(found.get().getUser().getUsername()).isEqualTo("testUser");
    }

    @Test
    public void findByIdJoinUser_존재하지않는_게시글_테스트() {
        // given : 존재하지 않는 id
        Long notExistId = 9999L;

        // when : 조회
        Optional<Board> found = boardJpaRepository.findByIdJoinUser(notExistId);

        // then : 비어 있음
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void findById_지연로딩이면_작성자는_프록시_테스트() {
        // given : 게시글 저장 후 영속성 컨텍스트 비우기
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("테스트글", user));
        em.flush();
        em.clear();

        // when : JOIN FETCH 없이 기본 findById 로 조회
        Board found = boardJpaRepository.findById(savedBoard.getId()).orElseThrow();

        // then : 작성자는 아직 채워지지 않은 프록시
        // (Board 의 user 가 LAZY 일 때 통과, EAGER 이면 실패)
        Assertions.assertThat(Hibernate.isInitialized(found.getUser())).isFalse();
    }

    // ===== findAllJoinUser (목록 조회 + 작성자) =====

    @Test
    public void update_게시글_수정_더티체킹_테스트() {
        // given : 게시글 저장과 수정할 내용
        User user = saveUser();
        Board savedBoard = boardJpaRepository.save(createBoard("수정 전 제목", user));
        BoardRequest.updateDto updateDto = new BoardRequest.updateDto();
        updateDto.setTitle("수정 후 제목");
        updateDto.setContent("수정 후 내용");

        // when : 엔티티의 값만 변경 (save 를 다시 호출하지 않음)
        savedBoard.update(updateDto);
        em.flush(); // 변경 감지로 UPDATE 쿼리 실행
        em.clear(); // 영속성 컨텍스트를 비워서 DB 값으로 확인

        // then : DB 에서 다시 조회하면 수정된 값
        Board found = boardJpaRepository.findById(savedBoard.getId()).orElseThrow();
        Assertions.assertThat(found.getTitle()).isEqualTo("수정 후 제목");
        Assertions.assertThat(found.getContent()).isEqualTo("수정 후 내용");
    }
}