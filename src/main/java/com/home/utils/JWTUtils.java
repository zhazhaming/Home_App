package com.home.utils;

import com.home.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/06/22:59
 */
@Component
public class JWTUtils {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    private Key getSecretKey() {
        String secret = jwtConfig.getSecret() != null ? jwtConfig.getSecret() : "Home_Encrypt_The_Token_To_Enhance_Confidentiality";
        return new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    public String generateToken(String subject, long expirationTime) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            LogUtils.error("JWT已过期: " + e.getMessage());
            throw e; // 重新抛出过期异常，让调用方知道具体原因
        } catch (io.jsonwebtoken.security.SignatureException e) {
            LogUtils.error("JWT签名验证失败: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            LogUtils.error("JWT格式错误: " + e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            LogUtils.error("不支持的JWT: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LogUtils.error("JWT参数错误: " + e.getMessage());
        } catch (Exception e) {
            LogUtils.error("JWT解析异常: " + e.getMessage());
        }
        return null;
    }

    public String getUserFromToken(String token){
        return validateToken (token).getSubject ();
    }

    public boolean isTokenExpired(String token){
        Claims claims = validateToken(token);
        if (claims == null) {
            // Token无效或解析失败,返回错误
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
    
    /**
     * 生成短效token（2小时）
     */
    public String generateShortToken(String subject) {
        return generateToken(subject, jwtConfig.getShortExpiration());
    }
    
    /**
     * 生成长效token（7天）
     */
    public String generateLongToken(String subject) {
        return generateToken(subject, jwtConfig.getLongExpiration());
    }
    
    /**
     * 生成刷新token（30天）
     */
    public String generateRefreshToken(String subject) {
        return generateToken(subject, jwtConfig.getRefreshExpiration());
    }
    
    /**
     * 获取token剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = validateToken(token);
            if (claims == null) {
                return 0;
            }
            Date expiration = claims.getExpiration();
            long remainingTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remainingTime);
        } catch (Exception e) {
            LogUtils.error("获取token剩余时间失败: " + e.getMessage());
            return 0;
        }
    }
}
