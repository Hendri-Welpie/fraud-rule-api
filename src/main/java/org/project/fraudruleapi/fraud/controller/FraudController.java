package org.project.fraudruleapi.fraud.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.model.FraudDetectionResponse;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1/api/fraud")
@RequiredArgsConstructor
public class FraudController implements FraudApi {

    private static final Set<String> VALID_SEVERITIES = Set.of("CRITICAL", "HIGH", "MEDIUM", "LOW");
    private final FraudService fraudService;

    @Override
    @CircuitBreaker(name = "fraudService", fallbackMethod = "fallbackValidate")
    @RateLimiter(name = "fraudApi")
    @PostMapping(path = "/transactions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<FraudDetectionResponse>> transactions(
            @Valid @RequestBody TransactionDto transaction) {
        return fraudService.validate(transaction)
                .map(response -> {
                    if (response.isFraud()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.created(URI.create("/v1/api/fraud/transactions/" + response.transactionId()))
                            .body(response);
                });
    }

    @Override
    @GetMapping("/flag-items")
    public Mono<ResponseEntity<List<FraudEntity>>> getFlaggedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return fraudService.getFlaggedItems(page, size)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/flag-item/{id}")
    public Mono<ResponseEntity<FraudEntity>> getFlaggedItem(@PathVariable Long id) {
        return fraudService.getFlaggedItem(id)
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public Mono<ResponseEntity<List<FraudEntity>>> getFraudByAccountId(
            @PathVariable Long accountId) {
        return fraudService.getFraudByAccountId(accountId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/severity/{severity}")
    public Mono<ResponseEntity<List<FraudEntity>>> getFraudBySeverity(
            @PathVariable String severity) {
        String normalizedSeverity = severity.toUpperCase().trim();
        if (!VALID_SEVERITIES.contains(normalizedSeverity)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return fraudService.getFraudBySeverity(normalizedSeverity)
                .collectList()
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<FraudDetectionResponse>> fallbackValidate(TransactionDto transaction, Throwable throwable) {
        return Mono.just(ResponseEntity.status(503)
                .body(FraudDetectionResponse.builder()
                        .transactionId(transaction.transactionId())
                        .isFraud(true)
                        .riskScore(100)
                        .severity("SUSPICIOUS")
                        .matchedRules(List.of("CIRCUIT_BREAKER_OPEN"))
                        .processingTimeMs(0L)
                        .build()));
    }
}