package org.project.fraudruleapi.shared.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingPatternLayoutTest {

    @Test
    void shouldMaskAccountId() {
        String input = "Processing \"account_id\": 123456 for transaction";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("\"account_id\": ***");
        assertThat(masked).doesNotContain("123456");
    }

    @Test
    void shouldMaskUserId() {
        String input = "User \"user_id\": 789 initiated transfer";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("\"user_id\": ***");
        assertThat(masked).doesNotContain("789");
    }

    @Test
    void shouldMaskBeneficiaryAccount() {
        String input = "\"beneficiary_account\": 987654";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("\"beneficiary_account\": ***");
        assertThat(masked).doesNotContain("987654");
    }

    @Test
    void shouldMaskIpAddress() {
        String input = "\"ip_address\": \"192.168.1.100\"";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("***.***.***.***");
        assertThat(masked).doesNotContain("192.168.1.100");
    }

    @Test
    void shouldMaskDeviceId() {
        String input = "\"device_id\": \"device-12345\"";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("\"device_id\": \"***\"");
        assertThat(masked).doesNotContain("device-12345");
    }

    @Test
    void shouldMaskGeoLocation() {
        String input = "\"geo_location\": \"-33.9249,18.4241\"";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("\"geo_location\": \"***\"");
        assertThat(masked).doesNotContain("-33.9249,18.4241");
    }

    @Test
    void shouldMaskCamelCaseAccountId() {
        String input = "TransactionDto(accountId=123456, amount=500)";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("accountId=***");
        assertThat(masked).doesNotContain("123456");
    }

    @Test
    void shouldMaskCamelCaseIpAddress() {
        String input = "TransactionDto(ipAddress=192.168.1.1, channel=WEB)";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("ipAddress=***");
        assertThat(masked).doesNotContain("192.168.1.1");
    }

    @Test
    void shouldMaskCamelCaseDeviceId() {
        String input = "TransactionDto(deviceId=DEV001, status=PENDING)";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).contains("deviceId=***");
        assertThat(masked).doesNotContain("DEV001");
    }

    @Test
    void shouldNotModifyMessageWithoutPii() {
        String input = "Processing transaction TXN-001 with amount 500.00";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).isEqualTo(input);
    }

    @Test
    void shouldHandleNullMessage() {
        assertThat(PiiMaskingPatternLayout.maskMessage(null)).isNull();
    }

    @Test
    void shouldHandleEmptyMessage() {
        assertThat(PiiMaskingPatternLayout.maskMessage("")).isEmpty();
    }

    @Test
    void shouldMaskMultiplePiiInSameMessage() {
        String input = "\"account_id\": 12345, \"ip_address\": \"10.0.0.1\", \"device_id\": \"dev-99\"";
        String masked = PiiMaskingPatternLayout.maskMessage(input);
        assertThat(masked).doesNotContain("12345");
        assertThat(masked).doesNotContain("10.0.0.1");
        assertThat(masked).doesNotContain("dev-99");
    }
}

