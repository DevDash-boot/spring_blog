package com.tenco.spring_blog._core.config;

import com.tenco.spring_blog._core.interceptor.*;
import com.tenco.spring_blog._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration  // 스프링 설정 클래스임을 표시(빈으로도 등록됨)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;  // 연관관계
    private final IpBlockInterceptor ipBlockInterceptor;
    private final SessionUserModelInterceptor sessionUserModelInterceptor;
    private final AdminInterceptor adminInterceptor;

    // 업로드 폴더의 파일을 /images/** 주소로 제공
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 폴더 경로를 file:///... 형식의 주소로 변환(운영체제에 맞게 자동 변환됨)
        String location = Paths.get(FileUtil.UPLOAD_DIR).toUri().toString();
        // 윈도우 기준 경로 : C:\Users\사용자명\blog_uploads\

        // 폴더 위치는 반드시 / 로 끝나야 그 안의 파일을 찾을 수 있다.
        if(!location.endsWith("/")){
            location = location + "/";
        }
        registry.addResourceHandler("/images/**").addResourceLocations(location);
    }

    // 내가 정의한 인터셉터를 설정 클래스에 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 화면에 사용자 정보 자동 주입 인터셉터
        registry.addInterceptor(sessionUserModelInterceptor).addPathPatterns("/**");    // 모든 경로
        registry.addInterceptor(ipBlockInterceptor).addPathPatterns("/**"); // IP 검사

        // 로그인 검사
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴을 지정
                .addPathPatterns("/user/**", "/board/**", "/reply/**", "/admin/**")
                // 인터셉터에서 제외할 URL 패턴을 지정
                // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미
                // 예 : /board/1, /board/123 등 상세보기는 로그인 없어도 접근 가능
                // /board/1/update 처럼 뒤에 경로가 있으면 제외 대상이 아니게 된다.
                .excludePathPatterns("/board/list", "/board/{id:\\d+}");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/admin/**"); // 관리자 검사
    }

    // 비밀번호 해싱 도구를 빈으로 등록 (필요한 곳에서 DI 가능)
    @Bean   // IoC 처리됨
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
