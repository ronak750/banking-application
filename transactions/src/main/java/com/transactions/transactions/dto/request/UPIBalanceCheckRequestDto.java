package com.transactions.transactions.dto.request;

import jakarta.validation.constraints.*;

public record UPIBalanceCheckRequestDto(
        @NotEmpty(message = "UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String upiId,

        @Digits(integer = 4, fraction = 0, message = "Pin must be a valid 4 digit number")
        @Min(value = 1000, message = "Pin must be a valid 4 digit number")
        @Max(value = 9999, message = "Pin must be a valid 4 digit number")
        int pin
) {
}
