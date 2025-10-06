package org.project.fraudruleapi.fraud.controller;

import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/v1/api/fraud")
@RequiredArgsConstructor
public class FraudController implements FraudApi {
    private final FraudService fraudService;

    @PostMapping(path = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> transactions(@RequestBody TransactionDto transaction) {
        return this.fraudService.validate(transaction)
                .then(Mono.fromCallable(() -> ResponseEntity.created(URI.create("/rules")).build()));
    }

    @GetMapping("/flag-items")
    public Mono<ResponseEntity<Page<FraudEntity>>> getFlaggedItems(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return this.fraudService.getFlaggedItems(page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/flag-item/{id}")
    public Mono<ResponseEntity<FraudEntity>> getFlaggedItem(@PathVariable("id") Long id) {
        return this.fraudService.getFlaggedItem(id)
                .map(ResponseEntity::ok);
    }
}