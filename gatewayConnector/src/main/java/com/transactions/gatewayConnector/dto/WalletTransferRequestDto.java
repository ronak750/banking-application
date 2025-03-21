package com.transactions.gatewayConnector.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record WalletTransferRequestDto(
        @NotEmpty(message = "Transaction Id cannot be empty")
        String transactionId,

        @Min(value = 1)
        Long fromWalletId,

        @Min(value = 1)
        @Digits(integer = 10, fraction = 0)
        Long toWalletId,

        @Digits(integer = 10, fraction = 2)
        @Min(value = 1, message = "Amount must be greater than 0")
        double amount
)
{

}
