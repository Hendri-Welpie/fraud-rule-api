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
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(FraudController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.enabled=true",
        "app.security.users[0].username=admin",
        "app.security.users[0].password=admin",
        "app.security.users[0].role=ADMIN",
        "app.security.users[1].username=analyst",
        "app.security.users[1].password=analyst",
        "app.security.users[1].role=ANALYST"
})
class SecurityConfigTest {

    @MockitoBean
    private AuditRepository auditRepository;

    @MockitoBean
    private FraudService fraudService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void unauthenticatedRequest_shouldReturn401() {
        webTestClient.get().uri("/v1/api/fraud/flag-items")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void admin_shouldAccessFraudEndpoints() {
        when(fraudService.getFlaggedItems(0, 50)).thenReturn(Flux.just(FraudEntity.builder().id(1L).build()));

        webTestClient
                .mutateWith(mockUser("admin").roles("ADMIN"))
                .get().uri("/v1/api/fraud/flag-items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void analyst_shouldAccessFraudReadEndpoints() {
        when(fraudService.getFlaggedItems(0, 50)).thenReturn(Flux.just(FraudEntity.builder().id(1L).build()));

        webTestClient
                .mutateWith(mockUser("analyst").roles("ANALYST"))
                .get().uri("/v1/api/fraud/flag-items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postWithoutAuth_shouldReturn401() {
        webTestClient.post().uri("/v1/api/fraud/transactions")
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
