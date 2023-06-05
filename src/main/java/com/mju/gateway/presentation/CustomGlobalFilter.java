package com.mju.gateway.presentation;

import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    private static final String SOCOA_SSO_TOKEN = "SOCOA-SSO-TOKEN";
    private static final String FIRST_LOGIN_PATH = "/user-service/login";

    //test
    @Value("${socoa.login.uri}")
    private String LOGIN_URI;
    @Value("${socoa.home.uri}")
    private String SOCOA_HOME_URI;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        log.info("{}", exchange.getRequest().getURI());
        if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION) ||
                exchange.getRequest().getCookies().containsKey(SOCOA_SSO_TOKEN)) {
            if (isFirstLogin(exchange)) {
                return firstLoginRequest(exchange);
            }
            return chain.filter(exchange);
        }

        return unAuthenticationRequest(exchange);
    }

    private boolean isFirstLogin(final ServerWebExchange exchange) {
        return exchange.getRequest().getURI().getPath().equals(FIRST_LOGIN_PATH);
    }

    private Mono<Void> firstLoginRequest(final ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create(SOCOA_HOME_URI));
        return response.setComplete();
    }

    private Mono<Void> unAuthenticationRequest(final ServerWebExchange exchange) {
        log.warn("UnAuthenticationRequest = {}", exchange.getRequest().getURI());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create(LOGIN_URI));
        return response.setComplete();
    }

    public int getOrder() {
        return -1;
    }
}
