package com.transactions.gatewayconnector.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record NeftTransferRequestDto(
        @NotEmpty(message = "Transaction ID cannot be null or empty")
        String transactionId,

        @NotEmpty(message = "From Account Number cannot be null or empty")
        String fromAccountNumber,

        @NotEmpty(message = "To Account Number cannot be null or empty")
        String toAccountNumber,

        @NotEmpty(message = "From IFSC Code cannot be null or empty")
        String fromIfscCode,

        @NotEmpty(message = "To IFSC Code cannot be null or empty")
        String toIfscCode,

        @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number")
        @Min(value = 1, message = "Amount must be greater than or equal to 1 Rs")
        @Max(value = 1_00_000, message = "Amount must be less than or equal 1 Lakh Rs")
        double amount
) {
}
