package com.mju.gateway.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor
public class JwtTokenUtils {

    private Key key;

    public JwtTokenUtils(String secretKey){
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 검증
    public boolean validateToken(String token){
        try {
            Claims claims = this.getTokenClaims(token).getBody();
            if(claims == null) return false;
            return claims.getExpiration().after(new Date());
        }catch(Exception e) {
            log.info("옳바르지 않은 JWT 토큰입니다.");
            return false;
        }
    }

    public Map<String, String> getUserInfo(String token){
        Map<String, String> userInfo = new HashMap<>();
        Claims claims = getTokenClaims(token).getBody();
        userInfo.put("clientIdx", claims.getSubject());
        userInfo.put("role", claims.get("role").toString());
        return userInfo;
    }

    // 토큰 속성 정보 추출
    private Jws<Claims> getTokenClaims(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (SecurityException e) {
            log.info("JWT 서명이 옳바르지 않습니다.");
        } catch (MalformedJwtException e) {
            log.info("JWT 토큰이 옳바르지 않습니다.");
        } catch (ExpiredJwtException e) {
            log.info("JWT 토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰 압축이 옳바르지 않습니다.");
            e.printStackTrace();
        }
        return null;
    }
}
