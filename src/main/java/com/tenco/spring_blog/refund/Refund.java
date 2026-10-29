package com.tenco.spring_blog.refund;

import com.tenco.spring_blog.payment.Payment;
import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "refund_tb")
@Getter
@NoArgsConstructor
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 환불 정책 (결제 1건에 환불 요청 1건) - 현재 규칙 : OneToOne
    // 추후 부분 환불 정책이 들어온다면 ManyToOne으로 해야한다.
    // 지금은 OneToOne으로 하려면 unique 제약 조건을 걸어준다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    // 사유자가 입력한 환불 사유
    @Column(nullable = false, length = 500)
    private String reason;

    // 환불 상태 : 최조 무조건 대기. 관리자 판단에 따라서 승인/거절로 업데이트
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    // 관리자 거절 사유
    @Column(length = 500)
    private String rejectReason;

    // 환불 요청 시간
    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;    // 마지막 변경 시간(승인, 거절 시간)

    @Builder
    public Refund(User user, Payment payment, String reason) {
        this.user = user;
        this.payment = payment;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
    }

    // 관리자가 아직 처리하지 않은 요청인지 확인
    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    // 승인 처리(트랜잭션 안에서 호출하면 더티채킹으로 UPDATE 처리 됨)
    public void approve() {
        this.status = RefundStatus.APPROVED;
    }

    // 거절 + 거절 사유
    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = rejectReason;
    }
}
