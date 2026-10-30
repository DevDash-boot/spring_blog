package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.error.Exception400;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.payment.PortOneApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// 환불 승인 처리 순서 관리 : 검사 -> 포트원 취소 -> 응답 확인 -> DB 반영
// 트랜잭션을 걸지 않는다. 포트원 응답을 기다리는 동안 쓰기 트랜잭션을 열어두지 않기 위해
// DB 작업은 트랜잭션이 있는 refundService에 맞긴다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneRefundService {
    private final RefundService refundService;
    private final PortOneApiClient portOneApiClient;

    public void approve(Long refundId) {
        // 1. 승인 가능 여부 검사(DB 조회만)
        RefundResponse.ApproveDto approveDto = refundService.checkApprove(refundId);
        // 2. 포트원 결제 취소 요청(외부 통신, 트랜잭션 밖)
        PaymentResponse.PortOneCancel portOneCancel;
        try {
            portOneCancel = portOneApiClient.cancelPayment(approveDto.getPaymentId(), approveDto.getAmount());
        } catch (RestClientResponseException e) {
            // 400, 401, 403, 404, 409 (포트원이 오류 상태 코드로 응답)
            throw new RuntimeException(e);
        } catch (RestClientException e) {
            // 502 - 응답을 받지 못함(연결 실패, 시간 초과)
            throw new RuntimeException(e);
        }
        // 3. 취소 완료 상태 확인(응답이 SUCCEEDED가 아니라면 DB를 바꾸지 않는다.)
        if (portOneCancel == null || portOneCancel.getCancellation() == null
                || !"SUCCEEDED".equals(portOneCancel.getCancellation().getStatus())) {
            throw new Exception400("결제 취소가 완료되지 않았습니다. 잠시 후 포트원 콘솔에서 확인해주세요.");
        }
        // 4. DB 반영(포인트 차감, 결제 CANCELED , 환불 APPROVE)
        refundService.approve(refundId);
    }
}
