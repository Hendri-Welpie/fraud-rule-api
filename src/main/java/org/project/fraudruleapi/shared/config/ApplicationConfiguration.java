package org.project.fraudruleapi.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.project.fraudruleapi.shared.validator.ValidCron;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("app")
public class ApplicationConfiguration {
    private Schedule schedule;

    @Getter
    @Setter
    public static class Schedule {
        @ValidCron(message = "Invalid or missing cron schedule")
        private String updateRules;
    }
}
