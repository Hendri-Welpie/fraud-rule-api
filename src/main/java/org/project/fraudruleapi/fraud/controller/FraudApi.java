package org.project.fraudruleapi.fraud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.model.PageResponse;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.ErrorResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Validated
public interface FraudApi {
    @Operation(summary = "transactions", description = "Evaluate and save transactions")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully saved transaction",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request does not adhere to standards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<Void>> transactions(@Valid final TransactionDto transaction);

    @Operation(summary = "getFlaggedItems", description = "Get all the fraud items")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved fraud items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Page.class))
                    )),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request does not adhere to standards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<PageResponse<FraudEntity>>> getFlaggedItems(@NotBlank final int page, @NotBlank final int size);

    @Operation(summary = "getFlaggedItem", description = "Get fraud item")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found fraud item",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FraudEntity.class)
                    )),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request does not adhere to standards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No entries found for request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<FraudEntity>> getFlaggedItem(@NotBlank final Long id);
}
