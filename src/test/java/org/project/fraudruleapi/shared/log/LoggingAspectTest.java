package org.project.fraudruleapi.shared.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        loggingAspect = new LoggingAspect();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("TestClass");
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1"});
    }

    @Test
    @SuppressWarnings("unchecked")
    void logAround_shouldHandleMonoReturnType() throws Throwable {
        MDC.put("traceId", "test-trace");
        Mono<String> monoResult = Mono.just("result");
        when(joinPoint.proceed()).thenReturn(monoResult);

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isInstanceOf(Mono.class);
        StepVerifier.create((Mono<String>) result)
                .expectNext("result")
                .verifyComplete();
        MDC.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void logAround_shouldHandleMonoEmpty() throws Throwable {
        MDC.put("traceId", "test-trace");
        when(joinPoint.proceed()).thenReturn(Mono.empty());

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isInstanceOf(Mono.class);
        StepVerifier.create((Mono<Object>) result)
                .verifyComplete();
        MDC.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void logAround_shouldHandleMonoError() throws Throwable {
        MDC.put("traceId", "test-trace");
        when(joinPoint.proceed()).thenReturn(Mono.error(new RuntimeException("test error")));

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isInstanceOf(Mono.class);
        StepVerifier.create((Mono<Object>) result)
                .expectError(RuntimeException.class)
                .verify();
        MDC.clear();
    }

    @Test
    void logAround_shouldHandleNonMonoReturnType() throws Throwable {
        when(joinPoint.proceed()).thenReturn("plain result");

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isEqualTo("plain result");
    }

    @Test
    void logAround_shouldHandleNullTraceId() throws Throwable {
        MDC.clear();
        when(joinPoint.proceed()).thenReturn("result");

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isEqualTo("result");
    }
}

