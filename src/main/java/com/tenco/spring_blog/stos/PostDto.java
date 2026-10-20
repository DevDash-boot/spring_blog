package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 서버측에 던질 데이터로 사용할 예정
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto {
    private Integer userId;
    private String title;
    private String body;
    private Integer id; // 보낼 때는 id를 모름. 저장 후 응답 시 할당

    @Builder
    public PostDto(Integer userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
    }
}
