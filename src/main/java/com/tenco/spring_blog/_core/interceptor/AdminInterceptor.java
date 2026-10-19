package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        if(modelAndView == null){
            return;
        }

        String viewName = modelAndView.getViewName();
        if(viewName != null && viewName.startsWith("redirect:")){
            return;
        }

        HttpSession session = request.getSession(false);
        if(session == null){
            return;
        }

        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto == null || !sessionUserDto.isAdmin()) {
            throw new Exception403("관리자만 접근할 수 있습니다.");
        }
    }
}
