package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import lombok.Data;

public class ReplyRequest {
    // 댓글 작성 dto
    @Data
    public static class saveDto {
        private Long boardId;   // 댓글을 달 게시글 id(input hidden으로 전달받음)
        private String comment;

        public void validate() {
            if (boardId == null) {
                throw new Exception400("게시글 정보가 없습니다.");
            }
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다.");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하로 입력해 주세요.");
            }
        }
        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }
}
