package com.transactions.transactions.dto.response;

public record UPIValidationResponseDTO(
        String upiId,
        Boolean isValid,
        String userName,
        Boolean isActive
) {
}
