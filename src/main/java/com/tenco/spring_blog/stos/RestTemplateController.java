package com.tenco.spring_blog.stos;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class RestTemplateController {
    // 테스트 주소 - http://localhost:8080/practice/todos/1
    @GetMapping("/practice/todos/{id}")
    public ResponseEntity<?> getTodoAsString(@PathVariable(name = "id") Integer id) {

        // 1. 요청할 외부 API 주소
        String url = "https://jsonplaceholder.typicode.com/todos/" + id;

        // 2. HTTP 요청을 보내는 도구 생성
        RestTemplate restTemplate = new RestTemplate();

        // 3. (Accept)요청 헤더 - 응답을 JSON으로 주세요.
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        // 외부 API 사용시 헤더에 던질 양식이 문서에 나와있는데 이를 보고 맞춰서 만들어줘야 한다.

        // 4. 요청 꾸러미(HttpEntity) =  헤더 + 바디(HTTP 요청 메세지 헤더와 바디를 만들어줌)
        HttpEntity request = new HttpEntity(headers);   // GET - 자원의 요청(바디가 없음)

        // 5. 요청 보내기 - exchange(주소, 메서드(GET, POST, DELETE ...), HTTP 메세지, 응답 바디 타입)
        ResponseEntity<TodoDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                TodoDto.class);

        // 6. 응답 확인
        System.out.println("상태 코드 확인 : " + response.getStatusCode());
        System.out.println("응답 헤더 확인 : " + response.getHeaders());
        System.out.println("바디 내용 확인 : " + response.getBody());

        // 7. 응답 DTO 형태 구축
        System.out.println("userID : " + response.getBody().getUserId());
        System.out.println("id : " + response.getBody().getId());
        System.out.println("title : " + response.getBody().getTitle());
        System.out.println("completed : " + response.getBody().isCompleted());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.getBody());
    }

    // 브라우저 ---> 우리 서버 요청 주소 설정
    // 테스트 주소 - http://localhost:8080/practice/posts
    @GetMapping("/practice/posts")
    public ResponseEntity<?> createPost() {
        // 1. 요청할 외부 API 주소
        String url = "https://jsonplaceholder.typicode.com/posts";

        // 2. HTTP 요청을 보내는 도구 생성
        RestTemplate restTemplate = new RestTemplate();

        // 3. 요청 헤더 - 응답을 JSON으로 주세요.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // ContentType : 보내는 데이터는 JSON
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 4. 보낼 데이터 구성
        PostDto postDto = PostDto.builder().userId(100).title("오늘은 S to S 배움").body("가나다라마바사").build();

        // 5. 바디 + 헤더
        HttpEntity<PostDto> request = new HttpEntity(postDto, headers);

        try {
            // 6. 통신 요청
            ResponseEntity<PostDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PostDto.class);
            // DTO 하나로 요청, 응답  --> 관리가 어려움 --> 요청 DTO / 응답 DTO 나눠서 설계.
            // 현재 코드에서 응답은 필요없어서 하나로 함

            // 6. 응답 확인
            System.out.println("상태 코드 확인 : " + response.getStatusCode());
            System.out.println("응답 헤더 확인 : " + response.getHeaders());
            System.out.println("바디 내용 확인 : " + response.getBody());

            return ResponseEntity.ok().body(response.getBody());
        } catch (HttpClientErrorException e) {
            // 외부 서버가 보낸 상태 코드와 오류 내용을 확인
            return ResponseEntity.status(e.getStatusCode()).body("외부 API 요청에 실패했습니다.");
        }
    }
}
