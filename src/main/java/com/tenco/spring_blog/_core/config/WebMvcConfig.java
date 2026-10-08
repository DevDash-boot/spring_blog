package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.LoginIntercepter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 스프링 설정 클래스임을 표시(빈으로도 등록됨)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginIntercepter loginIntercepter;  // 연관관계

    // 내가 정의한 인터셉터를 설정 클래스에 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // LoginIntercepter를 시스템에 등록
        registry.addInterceptor(loginIntercepter)
                // 인터셉터가 동작할 URL 패턴을 지정
                .addPathPatterns("/user/**", "/board/**")
                // 인터셉터에서 제외할 URL 패턴을 지정
                // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미
                // 예 : /board/1, /board/123 등 상세보기는 로그인 없어도 접근 가능
                // /board/1/update 처럼 뒤에 경로가 있으면 제외 대상이 아니게 된다.
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");
    }
}
