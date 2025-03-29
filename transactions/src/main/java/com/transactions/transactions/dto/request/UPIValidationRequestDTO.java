package com.transactions.transactions.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UPIValidationRequestDTO(
        @NotEmpty(message = "UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String upiId
) {
}
