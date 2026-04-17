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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/api/fraud")
@RequiredArgsConstructor
public class FraudController implements FraudApi {

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
    public Mono<ResponseEntity<List<FraudEntity>>> getFlaggedItems() {
        return fraudService.getFlaggedItems()
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/flag-item/{id}")
    public Mono<ResponseEntity<FraudEntity>> getFlaggedItem(@PathVariable("id") Long id) {
        return fraudService.getFlaggedItem(id)
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public Mono<ResponseEntity<List<FraudEntity>>> getFraudByAccountId(
            @PathVariable("accountId") Long accountId) {
        return fraudService.getFraudByAccountId(accountId)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @Override
    @GetMapping("/severity/{severity}")
    public Mono<ResponseEntity<List<FraudEntity>>> getFraudBySeverity(
            @PathVariable("severity") String severity) {
        return fraudService.getFraudBySeverity(severity)
                .collectList()
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<FraudDetectionResponse>> fallbackValidate(TransactionDto transaction, Throwable throwable) {
        return Mono.just(ResponseEntity.status(503)
                .body(FraudDetectionResponse.builder()
                        .transactionId(transaction.transactionId())
                        .isFraud(false)
                        .riskScore(0)
                        .severity("UNKNOWN")
                        .matchedRules(List.of())
                        .processingTimeMs(0L)
                        .build()));
    }
}