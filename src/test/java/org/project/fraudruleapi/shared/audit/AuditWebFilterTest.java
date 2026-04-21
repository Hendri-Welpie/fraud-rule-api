package org.project.fraudruleapi.shared.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditWebFilterTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditWebFilter auditWebFilter;

    @BeforeEach
    void setUp() {
        auditWebFilter = new AuditWebFilter(auditRepository);
    }

    @Test
    void filter_shouldSaveAuditEntry() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/v1/api/fraud/flag-items")
                .header("X-Trace-Id", "test-trace-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);

        WebFilterChain chain = filterExchange -> Mono.empty();

        when(auditRepository.save(any(AuditEntity.class)))
                .thenReturn(Mono.just(AuditEntity.builder().id(1L).build()));

        StepVerifier.create(auditWebFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<AuditEntity> captor = ArgumentCaptor.forClass(AuditEntity.class);
        verify(auditRepository).save(captor.capture());

        AuditEntity saved = captor.getValue();
        assertThat(saved.getHttpMethod()).isEqualTo("GET");
        assertThat(saved.getUri()).isEqualTo("/v1/api/fraud/flag-items");
        assertThat(saved.getTraceId()).isEqualTo("test-trace-123");
        assertThat(saved.getPrincipal()).isEqualTo("anonymous");
    }

    @Test
    void filter_shouldSkipActuatorEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = filterExchange -> Mono.empty();

        StepVerifier.create(auditWebFilter.filter(exchange, chain))
                .verifyComplete();

        verify(auditRepository, never()).save(any());
    }

    @Test
    void filter_shouldSkipSwaggerEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/swagger-ui/index.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = filterExchange -> Mono.empty();

        StepVerifier.create(auditWebFilter.filter(exchange, chain))
                .verifyComplete();

        verify(auditRepository, never()).save(any());
    }

    @Test
    void filter_shouldHandleRepositoryError() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/v1/api/fraud/transactions")
                .header("X-Trace-Id", "trace-err")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);

        WebFilterChain chain = filterExchange -> Mono.empty();

        when(auditRepository.save(any(AuditEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(auditWebFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_shouldCapturePostMethod() {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/v1/api/fraud/transactions")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.CREATED);

        WebFilterChain chain = filterExchange -> Mono.empty();

        when(auditRepository.save(any(AuditEntity.class)))
                .thenReturn(Mono.just(AuditEntity.builder().id(2L).build()));

        StepVerifier.create(auditWebFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<AuditEntity> captor = ArgumentCaptor.forClass(AuditEntity.class);
        verify(auditRepository).save(captor.capture());
        assertThat(captor.getValue().getHttpMethod()).isEqualTo("POST");
    }
}

