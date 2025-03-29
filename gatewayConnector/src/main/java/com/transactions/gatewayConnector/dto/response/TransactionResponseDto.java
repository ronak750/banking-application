package com.transactions.gatewayconnector.dto.response;

import com.transactions.gatewayconnector.dto.TransactionStatus;

import java.time.LocalDateTime;

public record TransactionResponseDto(
        String transactionId,
        double amount,
        TransactionStatus status,
        String remarks,
        LocalDateTime time
) {
}
