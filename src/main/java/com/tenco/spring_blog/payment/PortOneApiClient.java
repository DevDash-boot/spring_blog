package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// 포트원 서버와 HTTP 통신만 담당하는 클래스 (포트원 설정값도 이곳에서만 관리)
@Slf4j
@Component
@Getter
public class PortOneApiClient {
    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;
    @Value("${portone.api-secret}")
    private String apiSecret;

    private static final String PAYMENT_URL = "https://api.portone.io/payments";
    private static final RestTemplate restTemplate = new RestTemplate(); // 여러 요청에서 함께 사용해도 안전해서 하나로 재사용

    // 포트원 서버에 결제 단건 조회(GET)
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret); // PortOne 뒤 반드 시 공백 한 칸

        // GET 요청이라 헤더만 담는다.
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(PAYMENT_URL + "/" + paymentId,
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class);
        return response.getBody();
    }

    // 포트원 서버에 결제 취소 요청
    // amount를 넣으면 그 금액만큼 취소됨. 부분 환불이 가능함. amount를 넣지 않으면 전액 취소됨
    // 이번 로직은 결제 금액 전체를 넣는다.
    // 포트원 서버에 결제 취소 요청
    public PaymentResponse.PortOneCancel cancelPayment(String paymentId, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "amount", amount,
                "reason", "관리자 환불 승인"
        );
        HttpEntity<Map> request = new HttpEntity<>(body, headers);
        ResponseEntity<PaymentResponse.PortOneCancel> response =
                restTemplate.exchange(
                        PAYMENT_URL + "/" + paymentId + "/cancel",
                        HttpMethod.POST,
                        request,
                        PaymentResponse.PortOneCancel.class
                );

        return response.getBody();
    }
}
