package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@NoArgsConstructor  // 필수(JPA 엔티티 생성시)
@Table(name = "user_tb")
@Entity
@Data
@AllArgsConstructor
public class User {
    @Id // 기본키 PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    // 같은 사용자명을 두 번 가입할 수 없도록 unique 제약
    @Column(unique = true)
    private String username;

    // @Column(length = 500) // varchar(255)를 varchar(500)으로 변경해보기
    private String password;

    // 같은 email을 두 번 가입할 수 없도록 unique 제약
    @Column(unique = true)
    private String email;

    @CreationTimestamp  // now()
    private Timestamp createdAt;

    // id와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
