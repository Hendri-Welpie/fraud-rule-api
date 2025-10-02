package org.project.fraudruleapi.shared.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmupRunner implements CommandLineRunner {
    private final List<CacheWarmer> cacheWarmers;
    private static final String TRACE_ID = "traceId";

    @Override
    public void run(String... args) {
        String startupTraceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID, startupTraceId);
        cacheWarmers.forEach(warmer -> {
            try {
                warmer.warmUp();
            } catch (Exception e) {
                log.error("Failed warming cache with {}", warmer.getClass().getSimpleName(), e);
            }
        });
    }
}
