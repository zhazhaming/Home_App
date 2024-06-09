package com.home.filter;

import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author: zhazhaming
 * @Date: 2024/06/08/15:40
 */
public class MDCFilter extends OncePerRequestFilter {

    @Autowired
    LogUtils logUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        logUtil.setRequestId(requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            logUtil.clearRequestId();
        }
    }
}