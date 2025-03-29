package com.transactions.api.gateway.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginDto(
        @NotNull(message = "User Id cannot be null")
        Long userId,

        @NotEmpty(message = "Password cannot be null or empty")
        String password
) {

}
