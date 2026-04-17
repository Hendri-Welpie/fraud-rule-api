package org.project.fraudruleapi.fraud.evaluator.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.shared.enums.ConditionalType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionEvaluatorFactory {

    private final List<ConditionEvaluator> evaluators;
    private final Map<ConditionalType, ConditionEvaluator> evaluatorMap = new EnumMap<>(ConditionalType.class);

    @PostConstruct
    public void init() {
        evaluators.forEach(evaluator -> {
            ConditionalType type = evaluator.getSupportedType();
            if (evaluatorMap.containsKey(type)) {
                log.warn("Duplicate evaluator registered for type: {}. Overwriting.", type);
            }
            evaluatorMap.put(type, evaluator);
            log.debug("Registered evaluator for type: {}", type);
        });
        log.info("Initialized {} condition evaluators", evaluatorMap.size());
    }

    public Optional<ConditionEvaluator> getEvaluator(ConditionalType type) {
        return Optional.ofNullable(evaluatorMap.get(type));
    }

    public ConditionEvaluator getEvaluatorOrThrow(ConditionalType type) {
        return getEvaluator(type)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported condition type: " + type));
    }
}

