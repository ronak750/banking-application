package com.transactions.users.dtos;

import com.transactions.users.entities.AccountStatusEnum;
import lombok.Builder;

@Builder
public record UserResponseDTO(
        Long id,

        String name,

        String email,

        String mobileNumber,

        AccountStatusEnum status
        ) {
}
