package com.tenco.spring_blog.reply;

import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

/*
댓글 엔티티(단방향 연관관계)
- Reply -> Board (N : 1) : 댓글이 달린 게시글
- Reply -> User (N : 1) : 댓글 작성자
*/
@Getter
@NoArgsConstructor
@Table(name = "reply_tb")
@Entity
public class Reply {

    // id와 createdAt 은 자동으로 채워지므로 빌더에서 제외
    @Builder
    public Reply(String comment, User user, Board board) {
        this.comment = comment;
        this.user = user;
        this.board = board;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 500, nullable = false)
    private String comment;
    // pc -> db
    @CreationTimestamp
    private Timestamp createdAt;

    // 단뱡향 N : 1 - 외래키 user_id 를 가짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 단뱡향 N : 1 - 외래키 board_id 를 가짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 편의 메서드 - 댓글 작성자 본인인지 확인
    public boolean isOwner(Long userId) {
        if (userId == null) {
            return false; // 로그인 하지 않은 사용자
        }
        return this.user.getId().equals(userId);  // true (내 댓글) false (내 댓글 아님)
    }
}
