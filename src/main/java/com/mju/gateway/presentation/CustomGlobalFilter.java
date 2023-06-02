package com.mju.gateway.presentation;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
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
        try {
            log.info("global filter = {}", exchange.getRequest().getURI());
            MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();

            if (isUnAuthenticationRequest(cookies)) {
                return unAuthenticationRequest(exchange);
            }

            if (isFirstLogin(exchange)) {
                log.info("first login = {} token = {}", exchange.getRequest().getURI(), cookies.get(SOCOA_SSO_TOKEN));
                return firstLoginRequest(exchange);
            }

            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("exception {}", exchange.getRequest().getURI());
            e.printStackTrace();
            return unAuthenticationRequest(exchange);
        }
    }

    private static boolean isUnAuthenticationRequest(final MultiValueMap<String, HttpCookie> cookies) {
        return cookies.containsKey(SOCOA_SSO_TOKEN) == false;
    }

    private static boolean isFirstLogin(final ServerWebExchange exchange) {
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
