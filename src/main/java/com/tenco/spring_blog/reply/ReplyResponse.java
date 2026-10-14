package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class ReplyResponse {
    @Data
    // 댓글 목록 응답 Dto
    public static class ListDto {
        private Long id;
        private String username;
        private String comment;
        private String createdAt;
        private boolean isOwner;    // 로그인한 사용자가 작성자인지(삭제 버튼 표시용)

        public ListDto(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            if (reply.getUser() != null) {
                this.username = reply.getUser().getUsername();
            }
            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
            this.isOwner = reply.isOwner(sessionUserId);
        }
    }
}
