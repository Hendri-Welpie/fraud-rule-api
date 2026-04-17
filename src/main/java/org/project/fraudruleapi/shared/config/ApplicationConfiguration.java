package org.project.fraudruleapi.shared.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.project.fraudruleapi.shared.validator.ValidCron;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Getter
@Setter
@Validated
@ConfigurationProperties("app")
public class ApplicationConfiguration {

    @NotNull
    private Schedule schedule;

    @NotNull
    private FraudConfiguration fraud;

    @Getter
    @Setter
    public static class Schedule {
        @ValidCron(message = "Invalid or missing cron schedule")
        private String updateRules;
    }

    @Getter
    @Setter
    public static class FraudConfiguration {
        @NotNull
        private EvaluationConfig evaluation;

        @NotNull
        private VelocityConfig velocity;

        @NotNull
        private RiskConfig risk;

        @NotNull
        private AmountConfig amount;
    }

    @Getter
    @Setter
    public static class EvaluationConfig {
        @Min(1)
        private int parallelThreshold = 5;

        @Positive
        private long timeoutMs = 5000;

        @Positive
        private int maxConcurrentEvaluations = 100;
    }

    @Getter
    @Setter
    public static class VelocityConfig {
        private boolean enabled = true;

        @Positive
        private int windowSeconds = 60;

        @Positive
        private int maxTransactions = 10;
    }

    @Getter
    @Setter
    public static class RiskConfig {
        @Min(0)
        private int highThreshold = 80;

        @Min(0)
        private int mediumThreshold = 50;

        @Min(0)
        private int lowThreshold = 20;
    }

    @Getter
    @Setter
    public static class AmountConfig {
        @NotNull
        private BigDecimal highValueThreshold = new BigDecimal("1000000.00");

        @NotNull
        private BigDecimal suspiciousThreshold = new BigDecimal("500000.00");
    }
}
