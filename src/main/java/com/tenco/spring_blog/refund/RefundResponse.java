package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.MyDateUtil;
import lombok.Builder;
import lombok.Data;

public class RefundResponse {
    @Data
    public static class ListDto {
        private Long id;    // 환불 요청 PK
        private int amount;
        private String paymentId;
        private String reason;
        private String rejectReason;
        private String statusDisplay;
        private String requestedAt;
        private boolean approved;
        private boolean pending;
        private boolean rejected;

        @Builder
        public ListDto(Refund refund) {
            this.id = refund.getId();
            this.amount = refund.getPayment().getAmount();
            this.paymentId = refund.getPayment().getPaymentId();
            this.reason = refund.getReason();
            if (refund.getCreatedAt() != null) {
                this.requestedAt = MyDateUtil.timestampFormat(refund.getCreatedAt());
            }
            this.statusDisplay = refund.getStatus().getDisplayName();
            if (refund.getRejectReason() != null) {
                this.rejectReason = refund.getRejectReason();
            }
            this.approved = refund.getStatus() == RefundStatus.APPROVED;
            this.pending = refund.getStatus() == RefundStatus.PENDING;
            this.rejected = refund.getStatus() == RefundStatus.REJECTED;
        }
    }
}
