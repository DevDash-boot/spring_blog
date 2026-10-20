package com.tenco.spring_blog.stos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
// 외부 서버에서는 우리 서버에 불필요한 내용까지 데이터를 전달.
// 불필요 필드(선언이 안된) 데이터 무시(안전상 이유로 선언을 권장)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoDto {
    // JSON의 key 이름과 필드 이름이 같아야 값이 들어온다.
    private Integer userId;
    private Integer id;
    private String title;
    private boolean completed;
}
