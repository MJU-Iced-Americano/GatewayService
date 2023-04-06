package com.mju.gateway.config.filter;

import com.mju.gateway.config.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@RefreshScope // 설정 변경시 서버를 재실행
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final String TOKEN_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if(request.getHeaders().containsKey("Authorization")){
            String token = getAuthHeaderValue(request);

            if (StringUtils.hasText(token) && jwtTokenUtils.validateToken(token)){
                String id = addRequestUserInfoHeader(exchange, token);
                exchange = rewritePathMe(exchange, id);
            }
        }

        return chain.filter(exchange);
    }

    private String getAuthHeaderValue(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getOrEmpty("Authorization").get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private String addRequestUserInfoHeader(ServerWebExchange exchange, String token){
        Map<String, String> userInfo = jwtTokenUtils.getUserInfo(token);
        exchange.getRequest().mutate()
                .header("id", userInfo.get("id"))
                .header("role", userInfo.get("role"))
                .build();
        return userInfo.get("id");
    }

    private ServerWebExchange rewritePathMe(ServerWebExchange exchange, String id){
        String path = exchange.getRequest().getURI().getRawPath();
        String newPath = path.replaceAll("me", id);
        ServerHttpRequest request = exchange.getRequest().mutate().path(newPath).build();
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());
        return exchange.mutate().request(request).build();
    }

}
