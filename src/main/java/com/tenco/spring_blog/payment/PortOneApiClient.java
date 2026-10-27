package com.tenco.spring_blog.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate = new RestTemplate(); // 여러 요청에서 함께 사용해도 안전해서 하나로 재사용

    // 포트원 서버에 결제 단건 조회(GET)
    public PaymentResponse.PortOnePayment getPayment(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret); // PortOne 뒤 반드 시 공백 한 칸

        // GET 요청이라 바디어 없에 헤더만 담는다.
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(PAYMENT_URL + "/" + paymentId,
                HttpMethod.GET,
                request,
                PaymentResponse.PortOnePayment.class);
        return response.getBody();
    }
}
