package org.project.fraudruleapi.rules.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.project.fraudruleapi.rules.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rules")
public class RuleController implements RuleApi {
    private final RuleService ruleService;

    @PostMapping
    public Mono<ResponseEntity<Void>> create(@RequestBody final JsonNode ruleJson) {
        return this.ruleService.createRule(ruleJson)
                .map(ruleId -> ResponseEntity.created(URI.create("/api/v1/rules/" + ruleId)).build());
    }

    @GetMapping("/{ruleId}")
    public Mono<ResponseEntity<RuleDto>> getRule(@PathVariable final String ruleId) {
        return this.ruleService.findRule(ruleId)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<List<RuleDto>>> getAllRules() {
        return this.ruleService.findAllRules()
                .map(ResponseEntity.ok()::body);
    }

    @PutMapping("/{ruleId}")
    public Mono<ResponseEntity<Void>> updateRule(@PathVariable final String ruleId,
                                                 @RequestBody final RuleDto ruleDto) {
        return this.ruleService.updateRule(ruleId, ruleDto)
                .then(Mono.fromCallable(() -> ResponseEntity.ok().build()));
    }

    @DeleteMapping("/{ruleId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String ruleId) {
        return this.ruleService.deleteRule(ruleId)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }

    @GetMapping("/active")
    public Mono<ResponseEntity<RuleDto>> getActiveRule() {
        return this.ruleService.findActiveRule()
                .map(ResponseEntity::ok);
    }
}
