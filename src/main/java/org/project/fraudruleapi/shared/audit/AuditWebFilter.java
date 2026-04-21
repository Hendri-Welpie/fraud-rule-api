package org.project.fraudruleapi.shared.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class AuditWebFilter implements WebFilter {

    private final AuditRepository auditRepository;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();

        // Skip actuator and swagger endpoints
        if (uri.startsWith("/actuator") || uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs") || uri.startsWith("/webjars")) {
            return chain.filter(exchange);
        }

        String method = exchange.getRequest().getMethod().name();
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");

        return exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous")
                .flatMap(principal -> chain.filter(exchange)
                        .then(Mono.defer(() -> {
                            ServerHttpResponse response = exchange.getResponse();
                            HttpStatusCode statusCode = response.getStatusCode();
                            int status = statusCode != null ? statusCode.value() : 0;

                            AuditEntity audit = AuditEntity.builder()
                                    .traceId(traceId)
                                    .principal(principal)
                                    .httpMethod(method)
                                    .uri(uri)
                                    .responseStatus(status)
                                    .timestamp(LocalDateTime.now())
                                    .build();

                            try {
                                Mono<?> saveMono = auditRepository.save(audit);
                                if (saveMono == null) {
                                    return Mono.empty();
                                }
                                return saveMono
                                        .doOnError(e -> log.warn("Failed to save audit trail: {}", e.getMessage()))
                                        .onErrorResume(e -> Mono.empty())
                                        .then();
                            } catch (Exception e) {
                                log.warn("Failed to save audit trail: {}", e.getMessage());
                                return Mono.empty();
                            }
                        })));
    }
}

