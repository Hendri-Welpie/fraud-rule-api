package org.project.fraudruleapi.rules.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import org.project.fraudruleapi.rules.model.ErrorResponse;
import org.project.fraudruleapi.rules.model.RuleDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
public interface RuleApi {
    @Operation(summary = "create", description = "Create new rule")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully created new rule",
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
    Mono<ResponseEntity<Void>> create(JsonNode ruleJson);

    @Operation(summary = "getRule", description = "Get rule using ruleId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found rule",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RuleDto.class)
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
    Mono<ResponseEntity<RuleDto>> getRule(@NotBlank String ruleId);

    @Operation(summary = "getAllRules", description = "Get all the rules")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved rules",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = RuleDto.class))
                    )),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request does not adhere to standards",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<List<RuleDto>>> getAllRules();

    @Operation(summary = "updateRule", description = "Update rule using ruleId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated rule",
                    content = @Content
            ),
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
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request was already made and no action can be performed at this time",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<Void>> updateRule(@NotBlank String ruleId,
                                          RuleDto ruleDto);

    @Operation(summary = "delete", description = "Delete rule using ruleId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rule was deleted",
                    content = @Content
            ),
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
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "Request was already made and no action can be performed at this time",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    Mono<ResponseEntity<Void>> delete(@NotBlank String ruleId);
}
