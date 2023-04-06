package com.mju.gateway.config.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public JwtTokenUtils jwtTokenUtils(){
        return new JwtTokenUtils(secretKey);
    }

}
