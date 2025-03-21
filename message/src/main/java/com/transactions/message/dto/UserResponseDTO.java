package com.transactions.message.dto;

public record UserResponseDTO(
        Long id,

        String name,

        String email,

        String mobileNumber,

        AccountStatusEnum status
        ) {
}
