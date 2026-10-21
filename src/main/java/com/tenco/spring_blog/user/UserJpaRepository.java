package com.tenco.spring_blog.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    // 사용자명 조회(로그인용)
    // 비밀번호는 해시로 저장되어 있어 DB에서 비교할 수 없어 조건에 넣지 않아야 한다.
    @Query("""
        select distinct u from User u left join fetch u.roles where u.username = :username
        """)
    Optional<User> findByUsernameWithRoles(@Param("username")String username);

    // 사용자명으로 사용자 조회(중복체크용)
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username")String username);

    // 이메일 조회
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmail(@Param("email")String email);
}
