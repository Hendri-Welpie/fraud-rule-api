package org.project.fraudruleapi.shared.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceFilterTest {

    @Mock
    private WebFilterChain chain;

    private final TraceFilter traceFilter = new TraceFilter();

    @Test
    void filter_shouldUseHeaderTraceId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header("X-Trace-Id", "test-trace-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(traceFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_shouldGenerateTraceIdWhenMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(traceFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
    }
}
