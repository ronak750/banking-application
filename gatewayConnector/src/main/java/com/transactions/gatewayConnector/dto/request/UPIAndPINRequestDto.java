package com.transactions.gatewayConnector.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record UPIAndPINRequestDto(
        @NotEmpty(message = "UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String upiId,

        @Digits(integer = 4, fraction = 0, message = "Pin must be a valid 4 digit number")
        @Min(value = 1000, message = "Pin must be greater than equal to 1000")
        int pin
) {
}
