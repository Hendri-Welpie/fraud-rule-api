package org.project.fraudruleapi.shared.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(1)
public class RequestSizeLimitFilter implements WebFilter {

    private static final long MAX_CONTENT_LENGTH = 256 * 1024;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long contentLength = exchange.getRequest().getHeaders().getContentLength();
        if (contentLength > MAX_CONTENT_LENGTH) {
            log.warn("Request rejected: Content-Length {} exceeds limit of {} bytes",
                    contentLength, MAX_CONTENT_LENGTH);
            exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}

