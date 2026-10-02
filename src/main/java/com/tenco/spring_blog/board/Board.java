package com.tenco.spring_blog.board;

import com.tenco.spring_blog.util.MyDateUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
// 엔티티 클래스 만들기 : 데이터베이스 클래스 한 개를 자바 클래스로 그린 설계도
@Table(name = "board_tb")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id // 이 필드가 기본키임을 나타냄
    // 기본키 값을 자동으로 생성. IDENTITY 전략 : DB의 기본 설정을 따른다. AUTO_INCREMENT 기능 적용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 별도 어노테이션이 없으면 필드명이 컬럼명이 된다.
    private String title;
    private String content;
    private String username;

    // now()를 사용하지 않아도 자동으로 PC의 시간을 DB로 주입
    @CreationTimestamp
    private Timestamp createdAt;    // created_at 컬럼 : 스프링이 기본값 스네이크 케이스로 자동 변환해 줌

    // 비즈니스 로직을 위한 생성자 설계
    // id와 createdAt은 JPA가 자동으로 설정하므로 매개변수에서 제외
    @Builder
    public Board(String title, String content, String username){
        this.title = title;
        this.content = content;
        this.username = username;
    }

    // 자신의 상태값을 변경하는 메서드 추가(영속성 엔티티를 수정하는 메서드)
    public void update(BoardRequest.updateDto updateDto){
        // 비즈니스 규칙 검증
        updateDto.validate();
        // 영속 상태에 있는 엔티티의 필드값을 여기서 변경
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        // 변경 감지(Dirty Checking) 동작 과정
        // 1. 영속성 컨텍스트가 엔티티 최초 상태를 스냅샷으로 따로 보관
        // 2. 필드값 변경 시 현재 시점 상태와 스냅샷 비교
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리를 자동 생성
        // (UPDATE board_tb SET title = ?, content = ? WHERE id =?)
    }

    // 시간을 포맷하는 메서드를 추가
    public String getTime(){
        return MyDateUtil.timestampFormat(createdAt);
    }
}
