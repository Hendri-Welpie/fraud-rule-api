package org.project.fraudruleapi.fraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.project.fraudruleapi.shared.enums.ChannelType;
import org.project.fraudruleapi.shared.enums.StatusType;
import org.project.fraudruleapi.shared.enums.TransactionType;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Validated
@Builder
public record TransactionDto(
        @NotBlank(message = "Transaction ID cannot be null or blank")
        @Size(max = 64, message = "Transaction ID must not exceed 64 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Transaction ID contains invalid characters")
        @JsonProperty("transaction_id")
        String transactionId,

        @NotNull(message = "Account ID cannot be null")
        @Positive(message = "Account ID must be positive")
        @JsonProperty("account_id")
        Long accountId,

        @NotNull(message = "User ID cannot be null")
        @Positive(message = "User ID must be positive")
        @JsonProperty("user_id")
        Long userId,

        @NotBlank(message = "Currency cannot be null or blank")
        @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO code")
        @JsonProperty("currency")
        String currency,

        @NotNull(message = "Transfer amount cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Transfer amount must be greater than 0")
        @JsonProperty("amount")
        Double transferAmount,

        @NotNull(message = "Timestamp cannot be null")
        @PastOrPresent(message = "Timestamp cannot be in the future")
        @JsonProperty("timestamp")
        LocalDateTime timeStamp,

        @JsonProperty("transaction_type")
        TransactionType transactionType,

        @JsonProperty("channel")
        ChannelType channel,

        @NotBlank(message = "Merchant ID cannot be null or blank")
        @Size(max = 64, message = "Merchant ID must not exceed 64 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Merchant ID contains invalid characters")
        @JsonProperty("merchant_id")
        String merchantId,

        @NotBlank(message = "Merchant Name cannot be null or blank")
        @Size(max = 255, message = "Merchant Name must not exceed 255 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 .,'\\-&]+$", message = "Merchant Name contains invalid characters")
        @JsonProperty("merchant_name")
        String merchantName,

        @NotNull(message = "Account ID cannot be null")
        @Positive(message = "Account ID must be positive")
        @JsonProperty("beneficiary_account")
        Long beneficiaryAccount,

        @NotBlank(message = "IP Address cannot be null or blank")
        @Size(max = 45, message = "IP Address must not exceed 45 characters")
        @Pattern(regexp = "^[0-9a-fA-F.:]+$", message = "IP Address contains invalid characters")
        @JsonProperty("ip_address")
        String ipAddress,

        @NotBlank(message = "Device Id cannot be null or blank")
        @Size(max = 128, message = "Device ID must not exceed 128 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\-_:.]+$", message = "Device ID contains invalid characters")
        @JsonProperty("device_id")
        String deviceId,

        @NotBlank(message = "Location cannot be null or blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 .,\\-/]+$", message = "Location contains invalid characters")
        @JsonProperty("geo_location")
        String location,

        @JsonProperty("status")
        StatusType status
) {
}
