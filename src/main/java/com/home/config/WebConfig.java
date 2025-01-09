package com.home.config;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/06/11:49
 */
import com.home.Handler.JwtInterceptor;
import com.home.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    JWTUtils jwtUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor (jwtUtils))
                .addPathPatterns("/api/files/**"); // 只拦截文件上传接口

    }
}
