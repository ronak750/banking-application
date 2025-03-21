package com.transactions.transactions.dtos;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record WalletTransferRequestDto(
        @NotEmpty
        String transactionId,

        @Min(1)
        Long fromWalletId,

        @Min(1)
        @Digits(integer = 10, fraction = 0)
        Long toWalletId,

        @Digits(integer = 10, fraction = 2)
        @Min(1)
        double amount
)
{

}
