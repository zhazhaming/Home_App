package com.home.Handler;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/06/11:47
 */
import com.home.utils.JWTUtils;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import com.home.utils.RedisUtils;
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
    private RedisUtils redisUtils;

    @Autowired
    public JsonSerialization jsonSerialization;

    @Autowired
    public JwtInterceptor(@Lazy JWTUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LogUtils.info("JWT拦截器处理请求: " + request.getRequestURI());
        String token = request.getHeader("Authorization");
        LogUtils.info("获取到token：" + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            
            try {
                // 验证token是否过期
                if (!jwtUtils.isTokenExpired(token)) {
                    String userInfo = jwtUtils.getUserFromToken(token);
                    if (userInfo != null) {
                        // 验证token是否在Redis中存在（检查是否被注销）
                        if (isTokenValidInRedis(token, userInfo)) {
                            // 将用户信息存储到Request中，以便后续使用
                            request.setAttribute("userInfo", userInfo);
                            LogUtils.info("JWT验证成功，用户信息已设置");
                            return true;
                        } else {
                            LogUtils.error("JWT已被注销，请重新登录");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"登录已失效，请重新登录\"}");
                            return false;
                        }
                    } else {
                        LogUtils.error("JWT解析失败：无法获取用户信息");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"Token无效，请重新登录\"}");
                        return false;
                    }
                } else {
                    LogUtils.error("JWT已过期，请重新登录");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"登录已过期，请重新登录\"}");
                    return false;
                }
            } catch (Exception e) {
                LogUtils.error("JWT验证过程中发生异常: " + e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"Token验证失败，请重新登录\"}");
                return false;
            }
        }
        
        LogUtils.error("请求头中缺少有效的Authorization token");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"请先登录\"}");
        return false;
    }
    
    /**
     * 验证token是否在Redis中存在（检查是否被注销）
     * 简化版本：直接检查token是否在Redis中存在
     */
    private boolean isTokenValidInRedis(String token, String userInfo) {
        try {
            // 简化处理：直接检查token是否在Redis中存在
            // 这里我们遍历所有可能的Redis key来查找token
            // 实际项目中可以考虑优化，比如在token中存储用户ID
            
            // 检查长效token
            String longTokenKey = "user:token:" + extractUserIdFromUserInfo(userInfo);
            String longToken = redisUtils.get(longTokenKey);
            if (token.equals(longToken)) {
                LogUtils.info("Token在长效token中找到，验证通过");
                return true;
            }
            
            // 检查短效token
            String shortTokenKey = "user:token:shot:" + extractUserIdFromUserInfo(userInfo);
            String shortToken = redisUtils.get(shortTokenKey);
            if (token.equals(shortToken)) {
                LogUtils.info("Token在短效token中找到，验证通过");
                return true;
            }
            
            LogUtils.warn("Token在Redis中未找到，可能已被注销");
            return false;
        } catch (Exception e) {
            LogUtils.error("验证token在Redis中的有效性时发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从userInfo中提取用户ID
     * 简化版本：假设userInfo是序列化的JSON字符串
     */
    private String extractUserIdFromUserInfo(String userInfo) {
        try {
            // 简化处理：假设userInfo包含用户ID信息
            if (userInfo != null && userInfo.contains("\"id\"")) {
                // 简单的JSON解析，提取id字段
                int idStart = userInfo.indexOf("\"id\":") + 5;
                int idEnd = userInfo.indexOf(",", idStart);
                if (idEnd == -1) {
                    idEnd = userInfo.indexOf("}", idStart);
                }
                if (idStart < idEnd) {
                    String idStr = userInfo.substring(idStart, idEnd).trim();
                    // 移除可能的引号
                    return idStr.replace("\"", "");
                }
            }
            LogUtils.warn("无法从userInfo中提取用户ID: " + userInfo);
            return null;
        } catch (Exception e) {
            LogUtils.error("提取用户ID失败: " + e.getMessage(), e);
            return null;
        }
    }
}
