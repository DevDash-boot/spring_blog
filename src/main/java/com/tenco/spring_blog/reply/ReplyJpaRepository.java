package com.tenco.spring_blog.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyJpaRepository extends JpaRepository<Reply, Long> {
    // 게시글의 댓글 목록 조회(작성자와 함께 조회, 최신 순으로 조회)
    // join, left join, join fetch(inner)
    @Query("select r from Reply r join fetch r.user u where r.board.id = :boardId order by r.id asc")
    List<Reply> findAllByBoardIdJoinUser(@Param("boardId") Long boardId);

    // 댓글 일괄 삭제(게시글 삭제 전 사용)
    // ★ 주의 ★ @Query는 기본적으로 조회(SELECT)로 실행이 된다. 그래서 DELETE, UPDATE를 직접 쓸 때는 @Modifying을 사용
    @Modifying  // delete, update 시 반드시 선언
    @Query("delete from Reply r where r.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
