package org.project.fraudruleapi.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter fraudDetectionCounter(MeterRegistry registry) {
        return Counter.builder("fraud.detection.total")
                .description("Total number of fraud detections")
                .register(registry);
    }

    @Bean
    public Counter transactionProcessedCounter(MeterRegistry registry) {
        return Counter.builder("fraud.transaction.processed.total")
                .description("Total number of transactions processed")
                .register(registry);
    }

    @Bean
    public Timer fraudEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("fraud.evaluation.duration")
                .description("Time taken to evaluate fraud rules")
                .register(registry);
    }

    @Bean
    public Counter velocityCheckCounter(MeterRegistry registry) {
        return Counter.builder("fraud.velocity.check.total")
                .description("Total number of velocity checks performed")
                .register(registry);
    }
}
