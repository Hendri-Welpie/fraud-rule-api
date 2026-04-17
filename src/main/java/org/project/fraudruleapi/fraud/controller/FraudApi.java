package org.project.fraudruleapi.fraud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.project.fraudruleapi.fraud.entity.FraudEntity;
import org.project.fraudruleapi.fraud.model.FraudDetectionResponse;
import org.project.fraudruleapi.fraud.model.TransactionDto;
import org.project.fraudruleapi.rules.model.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@Tag(name = "Fraud API", description = "Fraud detection and management operations")
public interface FraudApi {

    @Operation(summary = "Validate Transaction",
            description = "Evaluate a transaction against fraud rules and return detection results")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction evaluated - fraud detected",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FraudDetectionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction saved successfully - no fraud detected",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FraudDetectionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Mono<ResponseEntity<FraudDetectionResponse>> transactions(@Valid TransactionDto transaction);

    @Operation(summary = "Get Flagged Items",
            description = "Retrieve all flagged fraud items with pagination")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved fraud items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FraudEntity.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Mono<ResponseEntity<List<FraudEntity>>> getFlaggedItems();

    @Operation(summary = "Get Flagged Item",
            description = "Retrieve a specific flagged fraud item by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found fraud item",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FraudEntity.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fraud item not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    Mono<ResponseEntity<FraudEntity>> getFlaggedItem(@NotNull Long id);

    @Operation(summary = "Get Fraud Items by Account ID",
            description = "Retrieve all fraud items for a specific account")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved fraud items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FraudEntity.class))
                    )
            )
    })
    Mono<ResponseEntity<List<FraudEntity>>> getFraudByAccountId(@NotNull Long accountId);

    @Operation(summary = "Get Fraud Items by Severity",
            description = "Retrieve fraud items filtered by severity level with pagination")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved fraud items",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FraudEntity.class))
                    )
            )
    })
    Mono<ResponseEntity<List<FraudEntity>>> getFraudBySeverity(@NotNull String severity);
}
