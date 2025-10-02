package org.project.fraudruleapi.shared.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class TraceFilter implements WebFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(HEADER_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(TRACE_ID, traceId);

        return chain.filter(exchange)
                .contextWrite(Context.of(TRACE_ID, traceId))
                .doFinally(signal -> MDC.remove(TRACE_ID));
    }
}