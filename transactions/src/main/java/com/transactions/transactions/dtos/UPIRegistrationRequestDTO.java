package com.transactions.transactions.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UPIRegistrationRequestDTO(
        Long userId,

        @NotEmpty(message = "UPI ID cannot be null or empty")
        @Email(message = "Invalid UPI ID")
        String upiId
) {
}
