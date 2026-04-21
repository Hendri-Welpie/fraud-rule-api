package org.project.fraudruleapi.fraud.evaluator.strategy;

import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.shared.enums.ConditionType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@Component
public class RegexEvaluator extends AbstractConditionEvaluator {

    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_PATTERN_LENGTH = 256;

    @Override
    public ConditionType getSupportedType() {
        return ConditionType.REGEX;
    }

    @Override
    public boolean evaluate(Condition condition, TransactionDto transaction) {
        Object fieldVal = getFieldValue(transaction, condition.field());
        if (fieldVal == null) return false;

        String regex = String.valueOf(condition.value());
        if (regex.length() > MAX_PATTERN_LENGTH) {
            log.warn("Regex pattern too long ({} chars), rejecting for safety", regex.length());
            return false;
        }

        try {
            Pattern pattern = PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
            return pattern.matcher(String.valueOf(fieldVal)).matches();
        } catch (PatternSyntaxException e) {
            log.error("Invalid regex pattern: {}", regex, e);
            return false;
        }
    }
}

