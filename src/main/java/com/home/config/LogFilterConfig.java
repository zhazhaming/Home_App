package com.home.config;

import com.home.filter.MDCFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zhazhaming
 * @Date: 2024/06/08/15:43
 * @Description: 这里是log过滤器的配置
 */
@Configuration
public class LogFilterConfig {
    @Bean
    public FilterRegistrationBean<MDCFilter> loggingFilter(){
        FilterRegistrationBean<MDCFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MDCFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
