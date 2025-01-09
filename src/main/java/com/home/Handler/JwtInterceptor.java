package com.home.Handler;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/06/11:47
 */
import com.home.service.FilesService;
import com.home.utils.JWTUtils;
import com.home.utils.LogUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JWTUtils jwtUtils;

    @Autowired
    public JwtInterceptor(@Lazy JWTUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            if (!jwtUtils.isTokenExpired(token)) {
                String userInfo = jwtUtils.getUserFromToken(token);
                // 你可以在这里将用户信息存储到ThreadLocal或Request中，以便后续使用
                request.setAttribute("userInfo", userInfo);
                return true;
            }else {
                LogUtils.error ("Token Expired，Please log in again");
                response.sendError (HttpServletResponse.SC_EXPECTATION_FAILED, "Token Expired，Please log in again");
                return false;
            }
        }
        LogUtils.error ("Token is Unauthorized");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return false;
    }
}
