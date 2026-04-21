package org.project.fraudruleapi.fraud.evaluator.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.shared.enums.ConditionType;
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
    private final Map<ConditionType, ConditionEvaluator> evaluatorMap = new EnumMap<>(ConditionType.class);

    @PostConstruct
    public void init() {
        evaluators.forEach(evaluator -> {
            ConditionType type = evaluator.getSupportedType();
            if (evaluatorMap.containsKey(type)) {
                log.warn("Duplicate evaluator registered for type: {}. Overwriting.", type);
            }
            evaluatorMap.put(type, evaluator);
            log.debug("Registered evaluator for type: {}", type);
        });
        log.info("Initialized {} condition evaluators", evaluatorMap.size());
    }

    public Optional<ConditionEvaluator> getEvaluator(ConditionType type) {
        return Optional.ofNullable(evaluatorMap.get(type));
    }

    public ConditionEvaluator getEvaluatorOrThrow(ConditionType type) {
        return getEvaluator(type)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported condition type: " + type));
    }
}

