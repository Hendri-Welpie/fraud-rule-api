package org.project.fraudruleapi.shared.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom logback layout that masks PII (Personally Identifiable Information)
 * in log output. Masks account IDs, IP addresses, device IDs, user IDs,
 * and beneficiary accounts.
 */
public class PiiMaskingPatternLayout extends PatternLayout {

    private static final List<MaskingRule> MASKING_RULES = new ArrayList<>();

    static {
        // Mask account_id in JSON: "account_id": 123456 -> "account_id": "***"
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?account_id\"?\\s*[:=]\\s*)\\d+"),
                "$1***"));

        // Mask user_id in JSON: "user_id": 789 -> "user_id": "***"
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?user_id\"?\\s*[:=]\\s*)\\d+"),
                "$1***"));

        // Mask beneficiary_account: "beneficiary_account": 987654 -> "beneficiary_account": "***"
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?beneficiary_account\"?\\s*[:=]\\s*)\\d+"),
                "$1***"));

        // Mask IP addresses: 192.168.1.100 -> ***.***.***.***
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?ip_address\"?\\s*[:=]\\s*\"?)\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
                "$1***.***.***.***"));

        // Mask device_id: "device_id": "device-12345" -> "device_id": "***"
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?device_id\"?\\s*[:=]\\s*)\"[^\"]+\""),
                "$1\"***\""));

        // Mask geo_location: "geo_location": "-33.9249,18.4241" -> "geo_location": "***"
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(\"?geo_location\"?\\s*[:=]\\s*)\"[^\"]+\""),
                "$1\"***\""));

        // Mask accountId in method args (camelCase): accountId=123456 -> accountId=***
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(accountId\\s*[:=]\\s*)\\d+"),
                "$1***"));

        // Mask ipAddress in method args: ipAddress=192.168.1.1 -> ipAddress=***
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(ipAddress\\s*[:=]\\s*)[^\\s,)]+"),
                "$1***"));

        // Mask deviceId in method args: deviceId=device-123 -> deviceId=***
        MASKING_RULES.add(new MaskingRule(
                Pattern.compile("(deviceId\\s*[:=]\\s*)[^\\s,)]+"),
                "$1***"));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return maskMessage(message);
    }

    static String maskMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String masked = message;
        for (MaskingRule rule : MASKING_RULES) {
            masked = rule.pattern().matcher(masked).replaceAll(rule.replacement());
        }
        return masked;
    }

    private record MaskingRule(Pattern pattern, String replacement) {
    }
}

