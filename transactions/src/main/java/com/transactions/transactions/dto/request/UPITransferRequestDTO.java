package com.transactions.transactions.dto.request;

import jakarta.validation.constraints.*;

public record UPITransferRequestDTO(
        @NotEmpty(message = "Transaction ID cannot be null or empty")
        String transactionId,

        @NotEmpty(message = "From UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String fromUpiId,

        @NotEmpty(message = "To UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String toUpiId,

        @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number")
        @Max(value = 1_00_000, message = "Amount must be less than or equal 1 Lakh Rs")
        @Min(value = 1, message = "Amount must be greater than or equal to 1 Rs")
        double amount,

        @Digits(integer = 4, fraction = 0, message = "Pin must be a valid 4 digit number")
        @Min(value = 1000, message = "Pin must be greater than or equal to 1000")
        int pin
) {
}
