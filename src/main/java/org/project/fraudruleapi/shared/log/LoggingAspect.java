package org.project.fraudruleapi.shared.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    private static final String TRACE_ID = "traceId";

    @Around("execution(* org.project.fraudruleapi..*(..))")
    public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get(TRACE_ID);
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.info("TraceId: {} - Request for {}.{}() with arguments[s]={}",
                traceId,
                className,
                methodName,
                Arrays.toString(joinPoint.getArgs()));

        Instant start = Instant.now();
        Object returnValue = joinPoint.proceed();

        if (returnValue instanceof Mono<?>) {
            return ((Mono<?>) returnValue)
                    .doOnSuccess(result -> {
                        if (result != null) {
                            log.info(
                                    "TraceId: {} - Response for {}.{} with Result = {}",
                                    traceId,
                                    className,
                                    methodName,
                                    result);
                        } else {
                            log.info(
                                    "TraceId: {} - Response for {}.{} completed with EMPTY result",
                                    traceId,
                                    className,
                                    methodName);
                        }
                    })

                    .doOnError(throwable -> log.error(
                            "TraceId: {} - Response for {}.{}() with arguments[s]={} failed with cause={}, message={}",
                            traceId,
                            className,
                            methodName,
                            Arrays.toString(joinPoint.getArgs()),
                            throwable != null ? throwable.getCause() : "Unknown Exception occurred",
                            throwable != null ? throwable.getMessage() : "Unknown"))
                    .doFinally(signal -> log.info(
                            "Time Taken = {} ms (signal: {})",
                            Duration.between(start, Instant.now()).toMillis(),
                            signal));
        }
        return returnValue;
    }
}
