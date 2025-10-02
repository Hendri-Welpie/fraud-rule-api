package org.project.fraudruleapi.fraud;

import org.junit.jupiter.api.Test;
import org.project.fraudruleapi.fraud.controller.FraudController;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(FraudController.class)
class FraudControllerTest {

    @MockitoBean
    private FraudService fraudService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void transactions_shouldReturnCreated() {
        TransactionDto tx = new TransactionDto(
                "tx123", 1L, 2L, "USD", 100.0,
                LocalDateTime.now(),
                TransactionType.DEPOSIT, ChannelType.WEB,
                "M1", "Merchant", 3L, "127.0.0.1", "D1", "NY", StatusType.PENDING
        );

        when(fraudService.validate(any(TransactionDto.class)))
                .thenReturn(Mono.empty());

        webTestClient.post().uri("/v1/api/fraud/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tx)
                .exchange()
                .expectStatus().isCreated();
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
}
