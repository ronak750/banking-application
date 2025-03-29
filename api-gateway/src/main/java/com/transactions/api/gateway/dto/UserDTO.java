package com.transactions.api.gateway.dto;


public record UserDTO(
        String name,

        String email,

        String mobileNumber,

        String password
) {
}
