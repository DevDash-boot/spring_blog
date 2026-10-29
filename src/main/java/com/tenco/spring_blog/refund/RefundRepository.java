package com.tenco.spring_blog.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    // 결제(payment.id)로 조회 기능 추가
    @Query("select r from Refund r where r.payment.id = :paymentPk")
    Optional<Refund> findByPaymentPk(@Param("paymentPk") Long paymentPk);

    // 사용자가 환불 요청한 결제 PK 목록(결제 내역 화면에 환불 버튼 표시로 사용)
    @Query("select r.payment.id from Refund r where r.user.id = :userId")
    List<Long> findPaymentPkListByUserId(@Param("userId") Long userId);

    // 로그인한 사용자의 환불 요청 내역 (결제 정보를 함께 조회, 최신 순으로 정렬)
    @Query("""
            select r from Refund r 
            join fetch r.payment 
            where r.user.id =:userId 
            order by r.id desc
            """)
    List<Refund> findAllByUserIdJoinPayment(@Param("userId") Long userId);

    // 관리자 - 전체 환불 요청 목록
    // 관리자 - 승인할 환불 요청 1건 조회

}
