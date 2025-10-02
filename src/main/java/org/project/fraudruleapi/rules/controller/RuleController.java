package org.project.fraudruleapi.rules.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.service.RuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rules")
public class RuleController  implements RuleApi {
    private final RuleService ruleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> create(@RequestBody final JsonNode ruleJson) {
        return this.ruleService.createRule(ruleJson)
                .then(Mono.fromCallable(() -> ResponseEntity.created(URI.create("/rules")).build()));
    }

    @GetMapping("/{ruleId}")
    public Mono<ResponseEntity<RuleDto>> getRule(@PathVariable("ruleId") final String ruleId) {
        return this.ruleService.findRule(ruleId)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<List<RuleDto>>> getAllRules() {
        return this.ruleService.findAllRules()
                .map(ResponseEntity.ok()::body);
    }

    @PutMapping("/{ruleId}")
    public Mono<ResponseEntity<Void>> updateRule(@PathVariable("ruleId") final String ruleId,
                                                 @RequestBody final RuleDto ruleDto) {
        return this.ruleService.updateRule(ruleId, ruleDto)
                .then(Mono.fromCallable(() -> ResponseEntity.ok().build()));
    }

    @DeleteMapping("/{ruleId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("ruleId") String ruleId) {
        return this.ruleService.deleteRule(ruleId)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }
}
