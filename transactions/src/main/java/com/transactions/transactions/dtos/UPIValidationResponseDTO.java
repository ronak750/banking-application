package com.transactions.transactions.dtos;

public record UPIValidationResponseDTO(
        String upiId,
        Boolean isValid,
        String userName,
        Boolean isActive
) {
}
