package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.fraud.controller.FraudController;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.model.FraudDetectionResponse;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.fraud.service.FraudService;
import org.project.fraudruleapi.shared.enums.ChannelType;
import org.project.fraudruleapi.shared.enums.StatusType;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.project.fraudruleapi.shared.exception.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(FraudController.class)
class FraudControllerTest {

    @MockitoBean
    private FraudService fraudService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void transactions_shouldReturnCreated_whenNoFraud() {
        TransactionDto tx = new TransactionDto(
                "tx123", 1L, 2L, "USD", 100.0,
                LocalDateTime.now(),
                TransactionType.DEPOSIT, ChannelType.WEB,
                "M1", "Merchant", 3L, "127.0.0.1", "D1", "NY", StatusType.PENDING
        );

        FraudDetectionResponse response = FraudDetectionResponse.builder()
                .transactionId("tx123")
                .isFraud(false)
                .riskScore(0)
                .severity("LOW")
                .matchedRules(List.of())
                .processingTimeMs(10)
                .build();

        when(fraudService.validate(any(TransactionDto.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post().uri("/v1/api/fraud/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tx)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void transactions_shouldReturnOk_whenFraudDetected() {
        TransactionDto tx = new TransactionDto(
                "tx123", 1L, 2L, "USD", 100.0,
                LocalDateTime.now(),
                TransactionType.DEPOSIT, ChannelType.WEB,
                "M1", "Merchant", 3L, "127.0.0.1", "D1", "NY", StatusType.PENDING
        );

        FraudDetectionResponse response = FraudDetectionResponse.builder()
                .transactionId("tx123")
                .isFraud(true)
                .riskScore(75)
                .severity("HIGH")
                .matchedRules(List.of("rule-001"))
                .processingTimeMs(15)
                .build();

        when(fraudService.validate(any(TransactionDto.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post().uri("/v1/api/fraud/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tx)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.isFraud").isEqualTo(true)
                .jsonPath("$.riskScore").isEqualTo(75)
                .jsonPath("$.severity").isEqualTo("HIGH");
    }

    @Test
    void getFlaggedItem_shouldReturnEntity() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").build();
        when(fraudService.getFlaggedItem(1L)).thenReturn(Mono.just(fraud));

        webTestClient.get().uri("/v1/api/fraud/flag-item/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transactionId").isEqualTo("t1");
    }

    @Test
    void getFlaggedItem_shouldReturnNotFound() {
        when(fraudService.getFlaggedItem(99L))
                .thenReturn(Mono.error(new ResourceNotFound("Fraud item not found")));

        webTestClient.get().uri("/v1/api/fraud/flag-item/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getFlaggedItems_shouldReturnList() {
        FraudEntity fraud1 = FraudEntity.builder().id(1L).transactionId("t1").build();
        FraudEntity fraud2 = FraudEntity.builder().id(2L).transactionId("t2").build();
        when(fraudService.getFlaggedItems()).thenReturn(Flux.just(fraud1, fraud2));

        webTestClient.get().uri("/v1/api/fraud/flag-items")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FraudEntity.class)
                .hasSize(2);
    }

    @Test
    void getFraudByAccountId_shouldReturnList() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").accountId(123L).build();
        when(fraudService.getFraudByAccountId(123L)).thenReturn(Flux.just(fraud));

        webTestClient.get().uri("/v1/api/fraud/account/123")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FraudEntity.class)
                .hasSize(1);
    }

    @Test
    void getFraudBySeverity_shouldReturnList() {
        FraudEntity fraud = FraudEntity.builder().id(1L).transactionId("t1").severity("HIGH").build();
        when(fraudService.getFraudBySeverity("HIGH")).thenReturn(Flux.just(fraud));

        webTestClient.get().uri("/v1/api/fraud/severity/HIGH")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FraudEntity.class)
                .hasSize(1);
    }
}
