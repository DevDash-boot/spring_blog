package com.tenco.spring_blog.board;

import com.tenco.spring_blog.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(BoardPersistRepository.class)
@DataJpaTest
public class BoardPersistRepositoryTest {

    @Autowired
    private BoardPersistRepository boardPersistRepository;

    @Test
    public void save_연관관계_포함_게시글_저장_테스트() {
        // given
        // 1. User 객체 생성.(실제로는 세션에서 가져온다.)
        User user = new User(1L, "testUser", "1234", "test@email.com", null);

        Board board = Board.builder()
                .title("테스트글")
                .content("테스트내용")
                .user(user)
                .build();

        // when
        Board savedBoard = boardPersistRepository.save(board);

        // then
        // 1. 자동 생성된 ID 값 확인
        Assertions.assertThat(savedBoard.getId()).isNotNull();
        Assertions.assertThat(savedBoard.getId()).isGreaterThan(0);

        // 2. 입력한 데이터가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getTitle()).isEqualTo("테스트글");
        Assertions.assertThat(savedBoard.getContent()).isEqualTo("테스트내용");

        // 3. 연관관계가 올바르게 저장되었는지 확인
        Assertions.assertThat(savedBoard.getUser()).isNotNull();
        Assertions.assertThat(savedBoard.getUser().getUsername()).isEqualTo("testUser");

        // 4. 원본 객체와 반환된 객체가 동일한 참조인지 확인
        Assertions.assertThat(board).isSameAs(savedBoard);
    }

    @Test
    public void delete_게시글_삭제_테스트() {
        // given
        // 1. User 객체 생성
        User user = new User(1L, "testUser", "1234", "test@email.com", null);

        // 2. 게시글 생성
        Board board = Board.builder()
                .title("삭제 테스트글")
                .content("삭제 테스트내용")
                .user(user)
                .build();

        // 3. 게시글 저장
        Board savedBoard = boardPersistRepository.save(board);

        // 저장된 게시글의 ID 확인
        Long boardId = savedBoard.getId();

        // when
        // 4. 게시글 삭제
        boardPersistRepository.deleteById(boardId);

        // then
        // 5. 삭제된 게시글 조회
        Board deletedBoard = boardPersistRepository.findById(boardId);

        // 6. 게시글이 삭제되어 존재하지 않는지 확인
        Assertions.assertThat(deletedBoard).isNull();
    }

}
