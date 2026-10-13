package com.tenco.spring_blog._core.interceptor;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// 세션의 로그인 정보를 화면 데이터(Model)에 담아주는 인터셉터
// 컨트롤러마다 model.addAttribute("sessionUser", sessionUserDto)를 쓰지 않아도 된다.
// 세션 전체가 아니라 sessionUserDto 하나만 화면에 전달된다.
@Component
public class SessionUserModelInterceptor implements HandlerInterceptor {
    // 뷰가 렌더링되기 직전에 호출
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        // 1. 화면을 반환하는 요청만 처리 : 데이터만 반환하는 요청은 동작시키지 않는다.(@ResponseBody, @RestController 등)
        if (modelAndView == null) {
            return;
        }

        // 2. 리다이렉트 응답은 화면을 그리지 않으므로 건너뜀
        String viewName = modelAndView.getViewName();
        if (viewName != null && viewName.startsWith("redirect:")) {
            return;
        }

        // 3. 세션이 있을 때만 조회(없으면 새로 만들지 않음)
        HttpSession session = request.getSession(false);  // false : 새로운 세션을 생성하지 않는다.
        if (session == null) {
            return;
        }

        // 4. 로그인 정보가 있으면 화면에 데이터 추가
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if (sessionUserDto != null) {
            modelAndView.addObject(Define.SESSION_USER, sessionUserDto);
        }
    }
}
