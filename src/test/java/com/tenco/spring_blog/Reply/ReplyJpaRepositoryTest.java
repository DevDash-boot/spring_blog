package com.tenco.spring_blog.Reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.reply.Reply;
import com.tenco.spring_blog.reply.ReplyJpaRepository;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReplyJpaRepositoryTest {

    @Autowired
    private ReplyJpaRepository replyJpaRepository;

    @Autowired
    private BoardJpaRepository boardJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // 테스트용 사용자 생성
    private User 사용자_생성() {
        User user = User.builder()
                .username("testuser") // 확인 필요: User에 username 필드가 있다고 추측
                .email("test@example.com") // 확인 필요: User에 email 필드가 있다고 추측
                .password("1234") // 확인 필요: User에 password 필드가 있다고 추측
                .build();

        return userJpaRepository.saveAndFlush(user);
    }

    // 테스트용 게시글 생성
    private Board 게시글_생성(User user, String title) {
        Board board = Board.builder()
                .title(title) // 확인 필요: Board에 title 필드가 있다고 추측
                .content("테스트 내용") // 확인 필요: Board에 content 필드가 있다고 추측
                .user(user) // 확인 필요: Board의 작성자 필드명이 user라고 추측
                .build();

        return boardJpaRepository.saveAndFlush(board);
    }

    // 테스트용 댓글 생성
    private Reply 댓글_생성(Board board, User user, String content) {
        Reply reply = Reply.builder()
                .comment(content) // 확인 필요: Reply에 content 필드가 있다고 추측
                .board(board) // 확인 필요: Reply의 게시글 필드명이 board라고 추측
                .user(user) // 확인 필요: Reply의 작성자 필드명이 user라고 추측
                .build();

        return replyJpaRepository.saveAndFlush(reply);
    }

    @Test
    @DisplayName("게시글 ID로 댓글 목록을 조회한다")
    void findAllByBoardIdJoinUser_댓글조회_테스트() {
        // given: 사용자, 게시글, 댓글 두 개를 저장한다.
        User user = 사용자_생성();
        Board board = 게시글_생성(user, "테스트 게시글");
        댓글_생성(board, user, "첫 번째 댓글");
        댓글_생성(board, user, "두 번째 댓글");

        // when: 게시글 ID로 댓글 목록을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 두 개가 조회되는지 확인한다.
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("댓글이 없는 게시글은 빈 목록을 반환한다")
    void findAllByBoardIdJoinUser_댓글없는_게시글_테스트() {
        // given: 댓글이 없는 게시글을 저장한다.
        User user = 사용자_생성();
        Board board = 게시글_생성(user, "댓글 없는 게시글");

        // when: 해당 게시글의 댓글 목록을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 조회 결과가 비어 있는지 확인한다.
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("댓글 조회 시 작성자가 함께 로딩된다")
    void findAllByBoardIdJoinUser_작성자_로딩_테스트() {
        // given: 사용자, 게시글, 댓글을 저장한다.
        User user = 사용자_생성();
        Board board = 게시글_생성(user, "로딩 테스트 게시글");
        댓글_생성(board, user, "로딩 테스트 댓글");

        // when: 영속성 컨텍스트를 비운 뒤 댓글을 다시 조회한다.
        entityManager.flush();
        entityManager.clear();

        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글의 user 연관 엔티티가 로딩되었는지 확인한다.
        PersistenceUnitUtil util =
                entityManagerFactory.getPersistenceUnitUtil();

        assertThat(result).hasSize(1);
        assertThat(util.isLoaded(result.get(0), "user")).isTrue();
    }

    @Test
    @DisplayName("댓글은 ID 오름차순으로 조회된다")
    void findAllByBoardIdJoinUser_정렬순서_테스트() {
        // given: 같은 게시글에 댓글 두 개를 순서대로 저장한다.
        User user = 사용자_생성();
        Board board = 게시글_생성(user, "정렬 테스트 게시글");
        Reply first = 댓글_생성(board, user, "첫 번째 댓글");
        Reply second = 댓글_생성(board, user, "두 번째 댓글");

        // when: 게시글 ID로 댓글 목록을 조회한다.
        List<Reply> result =
                replyJpaRepository.findAllByBoardIdJoinUser(board.getId());

        // then: 댓글 ID가 저장 순서대로 오름차순인지 확인한다.
        assertThat(result)
                .extracting(Reply::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("특정 게시글의 댓글만 일괄 삭제한다")
    void deleteByBoardId_댓글삭제_테스트() {
        // given: 두 게시글에 각각 댓글을 저장한다.
        User user = 사용자_생성();
        Board targetBoard = 게시글_생성(user, "삭제 대상 게시글");
        Board otherBoard = 게시글_생성(user, "유지 대상 게시글");

        댓글_생성(targetBoard, user, "삭제할 댓글");
        댓글_생성(targetBoard, user, "삭제할 댓글 2");
        댓글_생성(otherBoard, user, "유지할 댓글");

        // when: 대상 게시글의 댓글을 삭제하고 영속성 컨텍스트를 비운다.
        replyJpaRepository.deleteByBoardId(targetBoard.getId());
        entityManager.flush();
        entityManager.clear();

        // then: 대상 게시글 댓글은 없고 다른 게시글 댓글은 남아 있는지 확인한다.
        assertThat(
                replyJpaRepository.findAllByBoardIdJoinUser(targetBoard.getId())
        ).isEmpty();

        assertThat(
                replyJpaRepository.findAllByBoardIdJoinUser(otherBoard.getId())
        ).hasSize(1);
    }
}