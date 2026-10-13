package com.tenco.spring_blog.board;

/*
게시글 응답 DTO
- 서비스(트랜잭션) 안에서 엔티티를 DTO로 변환해서 반환
- 뷰는 엔티티가 아닌 DTO만 사용하므로 OSIV를 꺼도 지연 로딩 오류가 발생하지 않는다.
- 화면에 필요한 값만 담기 때문에 계층 간 결합도가 낮아진다.
*/

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Data;

public class BoardResponse {
    // 게시글 상세 응답 DTO
    @Data
    public static class DetailDto{
        private Long id;    // board 게시글 id
        private String title;
        private String content;
        private Long userId;
        private String username;
        private String createdAt;

        public DetailDto(Board board){
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // 지연 로딩 오류 방지를 위한 방어적 코드
            if(board.getUser() != null){
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    @Data
    public static class ListDto{
        private Long id;    // board 게시글 id
        private String title;
        private String username;
        private String createdAt;

        public ListDto(Board board){
            this.id = board.getId();
            this.title = board.getTitle();
            // JOIN FETCH로 이미 가져온 user를 사용할 예정
            if(board.getUser() != null){
                this.username = board.getUser().getUsername();
            }
                this.createdAt = board.getTime();
        }
    }

    // 게시글 수정 화면 응답 DTO
    @Data
    public static class updateFormDto{
        private Long id;
        private String title;
        private String content;

        public updateFormDto(Board board){
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }
}
