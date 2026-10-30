package com.tenco.spring_blog.payment;

import com.tenco.spring_blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// 결제 내역 엔티티(포트원 V2)
@Getter
@NoArgsConstructor
@Entity
@Table(name = "payment_tb")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 우리 서버가 발급한 결제 건 식별자(유니크 - 중복 지급 방지)
    @Column(unique = true, nullable = false, length = 100)
    private String paymentId;

    // PG사 거래 번호(결제가 완료 되면 포트원 사이트에서 응답을 받음. 추후 추적용)
    private String pgTxId;

    // 결제한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 결제 예정 금액(실제 결제는 포트원에서 처리 -> 응답 -> 실제 결제 금액과 우리 사이트의 결제 금액 비교)
    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @CreationTimestamp
    private Timestamp createdAt;    // db에 기록용

    @Builder
    public Payment(String paymentId, User user, int amount) {
        this.paymentId = paymentId;
        this.user = user;
        this.amount = amount;
        this.status = PaymentStatus.READY;  // 처음에는 항상 결제 예정
    }

    // 편의 메서드
    // 결제를 요청한 사용자인지 확인
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // 이미 처리(포인트 지급)된 결제인지 확인
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    // 검증 통과 후 결제 완료로 변경 처리(더티 채킹)
    public void markPaid(String pgTxId) {
        this.status = PaymentStatus.PAID;   // 결제 상태를 완료로 변경
        this.pgTxId = pgTxId;   // null 값 허용
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }
}