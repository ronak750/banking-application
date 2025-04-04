package com.transactions.transactions.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record WalletTransferRequestDto(
        @NotEmpty
        String transactionId,

        @Min(value = 1, message = "From wallet id must be greater than or equal to 1")
        Long fromWalletId,

        @Min(value = 1, message = "To wallet id must be greater than or equal to 1")
        @Digits(integer = 10, fraction = 0)
        Long toWalletId,

        @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number")
        @Min(value = 1, message = "Amount must be greater than or equal to 1 Rs")
        double amount
)
{

}
