package com.transactions.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserDTO(
        @NotEmpty(message = "Name cannot be null or empty")
        @Size(min = 5, max = 50, message = "Name must be between 5 and 50 characters")
        String name,

        @NotEmpty(message = "Email cannot be null or empty")
        @Email(message = "Invalid email address")
        String email,

        @NotEmpty(message = "Mobile number cannot be null or empty")
        @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
        String mobileNumber,

        @NotEmpty(message = "Password cannot be null or empty")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        String password
        ) {
}
