package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 복합 유니크 제약(UniqueConstraint) : 한 사용자는 같은 역할을 한 번만 가진다.
// (1, ADMIN), (1, USER), (2,USER) 가능. 만약 (1, USER)를 한 번 더 넣으면 DB가 거부
// columnNames : 자바 필드명이 아니라 DB 컬럼명을 써야한다.
// user_id는 이 클래스에 필드로 없지만 User 엔터티에서 @JoinColumn(name = "user_id)를 설정하면 자동으로 컬럼이 만들어진다.
@Table(name = "user_role_tb", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role"})})
@NoArgsConstructor
@Getter
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // enum을 숫자가 아닌 문자열로 사용(ADMIN, USER로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    public UserRole(Role role) {
        this.role = role;
    }
}
