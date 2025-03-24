package com.transactions.api.gateway.dtos;


public record UserDTO(
        String name,

        String email,

        String mobileNumber,

        String password
) {
}
