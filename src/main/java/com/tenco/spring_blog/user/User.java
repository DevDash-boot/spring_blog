package com.tenco.spring_blog.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    // 프로필 이미지 파일 이름(선택 사항이므로 null 가능)
    private String profileImage;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean certification = false;

    // 사용자의 역할 목록(User ---- 1 : N ---- UserRole, 단방향)
    // [cascade, orphanRemoval] : 두 옵션은 부모 쪽의 @OneToMany 컬렉션에 붙인다. User가 List<UserRole>을 가지고 있다.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false)
    private List<UserRole> roles = new ArrayList<>();

    // 가입 경로(DB에는 LOCAL, KAKAO 문자열로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'LOCAL'")   // 문자열이므로 안에 작은 따옴표 필수
    private OAuthProvider provider;

    // 역할 추가(회원가입 시 기본 USER 부여, 관리자는 직접 지정)
    public void addRole(Role role){
        // 이미 가진 역할이면 추가하지 않음(코드에서 1차 방어)
        if(hasRole(role)){
            return;
        }
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 해당 역할을 가지고 있는지 확인
    public boolean hasRole(Role role){
        for(UserRole u : this.roles){
            if(u.getRole() == role){
                return true;    // 찾으면 바로 true
            }
        }
        return false;   // 끝까지 없으면 false
    }

    // 로그인 한 사용자의 관리자 여부 확인 메서드
    public boolean isAdmin(){
        return hasRole(Role.ADMIN); // 고정값 : 관리자 여부인지만 확인
    }

    // id와 createdAt은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public User(String username, String password, String email, String profileImage, OAuthProvider provider) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.provider = (provider == null) ? OAuthProvider.LOCAL : provider;
    }

    // 프로필 이미지 변경(null 이면 삭제) - 트랜잭션 안에서 호출하면 더티 체킹이 발생됨
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void update(String password) {
        this.password = password;
    }

    // 프로필 경로
    public String getProfilePath(){
        if(this.profileImage == null){
            return null;
        }
        // 카카오 소셜 로그인 시 http로 시작하는 이미지가 들어온다.
        if(this.profileImage.startsWith("http")){
            return this.profileImage;
        }
        // 로컬 사용자의 이미지
        return "/images/" + this.profileImage;
    }
}
