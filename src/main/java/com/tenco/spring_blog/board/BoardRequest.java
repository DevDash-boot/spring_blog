package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.user.User;
import lombok.Data;

public class BoardRequest {
    @Data
    public static class saveDto{
        private String title;
        private String content;

        // 검증 메서드(선택 사항)
        public void validate(){
            if(title == null || title.trim().isEmpty()){
                throw new Exception400("제목은 필수입니다.");
            }
            if(content == null || content.trim().isEmpty()){
                throw new Exception400("내용은 필수입니다.");
            }
        }

        // DTO에서 Entity로 변환한는 편의 메서드 설계
        public Board toEntity(User user){
            return Board.builder()
                    .title(this.title)
                    .content(this.content)
                    .user(user) // 세션에서 가져온 user 객체 설정
                    .build();
        }
    }

    @Data
    public static class updateDto{
        private String title;
        private String content;

        // 검증 메서드(선택 사항)
        public void validate(){
            if(title == null || title.trim().isEmpty()){
                throw new Exception400("제목은 필수입니다.");
            }
            if(content == null || content.trim().isEmpty()){
                throw new Exception400("내용은 필수입니다.");
            }
        }
    }
}
