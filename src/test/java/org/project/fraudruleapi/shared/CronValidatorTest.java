package org.project.fraudruleapi.shared;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.shared.validator.CronValidator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CronValidatorTest {
    private final CronValidator validator = new CronValidator();

    @Test
    void isValid_shouldReturnTrue_forValidCron() {
        assertThat(validator.isValid("0 0 * * * *", null)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_forInvalidCron() {
        assertThat(validator.isValid("invalid-cron", null)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenEmpty() {
        assertThat(validator.isValid("", null)).isFalse();
    }
}
