package org.project.fraudruleapi.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsConfigTest {

    private final MetricsConfig metricsConfig = new MetricsConfig();
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Test
    void fraudDetectionCounter_shouldBeRegistered() {
        Counter counter = metricsConfig.fraudDetectionCounter(registry);
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("fraud.detection.total");
    }

    @Test
    void transactionProcessedCounter_shouldBeRegistered() {
        Counter counter = metricsConfig.transactionProcessedCounter(registry);
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("fraud.transaction.processed.total");
    }

    @Test
    void fraudEvaluationTimer_shouldBeRegistered() {
        Timer timer = metricsConfig.fraudEvaluationTimer(registry);
        assertThat(timer).isNotNull();
        assertThat(timer.getId().getName()).isEqualTo("fraud.evaluation.duration");
    }

    @Test
    void velocityCheckCounter_shouldBeRegistered() {
        Counter counter = metricsConfig.velocityCheckCounter(registry);
        assertThat(counter).isNotNull();
        assertThat(counter.getId().getName()).isEqualTo("fraud.velocity.check.total");
    }
}

