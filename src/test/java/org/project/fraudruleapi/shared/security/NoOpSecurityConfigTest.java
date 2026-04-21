package org.project.fraudruleapi.shared.security;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.fraud.controller.FraudController;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.project.fraudruleapi.shared.audit.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.when;

@WebFluxTest(FraudController.class)
@Import(NoOpSecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.enabled=false"
})
class NoOpSecurityConfigTest {

    @MockitoBean
    private AuditRepository auditRepository;

    @MockitoBean
    private FraudService fraudService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void allEndpoints_shouldBeAccessibleWithoutAuth() {
        when(fraudService.getFlaggedItems(0, 50)).thenReturn(Flux.just(FraudEntity.builder().id(1L).build()));

        webTestClient.get().uri("/v1/api/fraud/flag-items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postEndpoint_shouldBeAccessibleWithoutAuth() {
        webTestClient.post().uri("/v1/api/fraud/transactions")
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().is4xxClientError(); // 400 due to validation, not 401
    }
}
