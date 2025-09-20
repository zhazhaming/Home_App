package com.home.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 * @Author: zhazhaming
 * @Date: 2025/01/08
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    private String secret;
    private long shortExpiration = 7200;      // 2小时
    private long longExpiration = 604800;     // 7天
    private long refreshExpiration = 2592000; // 30天
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getShortExpiration() {
        return shortExpiration;
    }
    
    public void setShortExpiration(long shortExpiration) {
        this.shortExpiration = shortExpiration;
    }
    
    public long getLongExpiration() {
        return longExpiration;
    }
    
    public void setLongExpiration(long longExpiration) {
        this.longExpiration = longExpiration;
    }
    
    public long getRefreshExpiration() {
        return refreshExpiration;
    }
    
    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
}
