package org.project.fraudruleapi.fraud.evaluator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.fraudruleapi.fraud.evaluator.strategy.ConditionEvaluator;
import org.project.fraudruleapi.fraud.evaluator.strategy.ConditionEvaluatorFactory;
import org.project.fraudruleapi.fraud.model.Condition;
import org.project.fraudruleapi.fraud.model.EvaluationResult;
import org.project.fraudruleapi.fraud.model.RuleDefinition;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.shared.config.ApplicationConfiguration;
import org.project.fraudruleapi.shared.exception.ConversionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudEvaluator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConditionEvaluatorFactory evaluatorFactory;
    private final ApplicationConfiguration config;

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public List<RuleDefinition> getRules(final RuleDto ruleDto) {
        try {
            JsonNode rulesNode = ruleDto.getData().get("rules");
            if (rulesNode == null || rulesNode.isNull()) {
                log.warn("No rules found in RuleDto");
                return List.of();
            }
            return objectMapper.readValue(rulesNode.traverse(), new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to parse rules from RuleDto", e);
            throw new ConversionException("Unable to convert rules");
        }
    }

    public List<EvaluationResult> evaluateAllRules(List<RuleDefinition> rules, TransactionDto transaction) {
        int parallelThreshold = config.getFraud().getEvaluation().getParallelThreshold();
        long timeoutMs = config.getFraud().getEvaluation().getTimeoutMs();

        if (rules.size() >= parallelThreshold) {
            return evaluateParallel(rules, transaction, timeoutMs);
        }
        return evaluateSequential(rules, transaction);
    }

    private List<EvaluationResult> evaluateSequential(List<RuleDefinition> rules, TransactionDto transaction) {
        Instant start = Instant.now();
        List<EvaluationResult> results = rules.stream()
                .filter(rule -> evaluateCondition(rule.condition(), transaction))
                .map(rule -> EvaluationResult.builder()
                        .ruleId(rule.id())
                        .ruleName(rule.name())
                        .description(rule.description())
                        .matched(true)
                        .evaluationTimeMs(Duration.between(start, Instant.now()).toMillis())
                        .build())
                .collect(Collectors.toList());

        log.debug("Sequential evaluation of {} rules completed in {}ms, {} matched",
                rules.size(), Duration.between(start, Instant.now()).toMillis(), results.size());
        return results;
    }

    private List<EvaluationResult> evaluateParallel(List<RuleDefinition> rules,
                                                     TransactionDto transaction,
                                                     long timeoutMs) {
        Instant start = Instant.now();

        List<CompletableFuture<EvaluationResult>> futures = rules.stream()
                .map(rule -> CompletableFuture.supplyAsync(() -> {
                    Instant ruleStart = Instant.now();
                    boolean matched = evaluateCondition(rule.condition(), transaction);
                    return EvaluationResult.builder()
                            .ruleId(rule.id())
                            .ruleName(rule.name())
                            .description(rule.description())
                            .matched(matched)
                            .evaluationTimeMs(Duration.between(ruleStart, Instant.now()).toMillis())
                            .build();
                }, virtualExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Parallel evaluation timed out or failed, some results may be incomplete", e);
        }

        List<EvaluationResult> results = futures.stream()
                .filter(f -> f.isDone() && !f.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .filter(EvaluationResult::matched)
                .collect(Collectors.toList());

        log.debug("Parallel evaluation of {} rules completed in {}ms, {} matched",
                rules.size(), Duration.between(start, Instant.now()).toMillis(), results.size());
        return results;
    }

    public boolean evaluateCondition(Condition cond, TransactionDto transactionDto) {
        if (cond == null) {
            log.warn("Null condition received, returning false");
            return false;
        }

        return switch (cond.type()) {
            case AND -> evaluateAnd(cond, transactionDto);
            case OR -> evaluateOr(cond, transactionDto);
            case NOT -> evaluateNot(cond, transactionDto);
            default -> evaluateSimpleCondition(cond, transactionDto);
        };
    }

    private boolean evaluateAnd(Condition cond, TransactionDto transactionDto) {
        if (cond.operands() == null || cond.operands().isEmpty()) {
            return true; // Empty AND is true
        }
        return cond.operands().stream()
                .allMatch(op -> evaluateCondition(op, transactionDto));
    }

    private boolean evaluateOr(Condition cond, TransactionDto transactionDto) {
        if (cond.operands() == null || cond.operands().isEmpty()) {
            return false; // Empty OR is false
        }
        return cond.operands().stream()
                .anyMatch(op -> evaluateCondition(op, transactionDto));
    }

    private boolean evaluateNot(Condition cond, TransactionDto transactionDto) {
        if (cond.operands() == null || cond.operands().isEmpty()) {
            return true; // NOT of empty is true
        }
        return !evaluateCondition(cond.operands().getFirst(), transactionDto);
    }

    private boolean evaluateSimpleCondition(Condition cond, TransactionDto transactionDto) {
        ConditionEvaluator evaluator = evaluatorFactory.getEvaluatorOrThrow(cond.type());
        return evaluator.evaluate(cond, transactionDto);
    }

    public int calculateRiskScore(List<EvaluationResult> matchedRules) {
        if (matchedRules == null || matchedRules.isEmpty()) {
            return 0;
        }
        return Math.min(matchedRules.size() * 25, 100);
    }

    public String determineSeverity(int riskScore) {
        var riskConfig = config.getFraud().getRisk();
        if (riskScore >= riskConfig.getHighThreshold()) {
            return "CRITICAL";
        } else if (riskScore >= riskConfig.getMediumThreshold()) {
            return "HIGH";
        } else if (riskScore >= riskConfig.getLowThreshold()) {
            return "MEDIUM";
        }
        return "LOW";
    }
}

