package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// 특정 IP를 차단하는 인터셉터를 구현. 조원들 차단
@Component
public class IpBlockInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String blockIp = request.getRemoteAddr();
        if(blockIp.equals("192.168.5.17") ||
                blockIp.equals("192.168.5.18") || blockIp.equals("192.168.7.232")){
            throw new Exception403("차단된 IP입니다.");
        }
        return true;
    }
}
