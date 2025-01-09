package com.home.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2024/08/06/22:59
 */
@Component
public class JWTUtils {
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String subject, long expirationTime) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date (System.currentTimeMillis ()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }catch (io.jsonwebtoken.security.SignatureException e){
            LogUtils.error ("Token signature does not match locally computed signature.");
        } catch (Exception e) {
           LogUtils.error ("Error parsing token: " + e.getMessage());
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
}
