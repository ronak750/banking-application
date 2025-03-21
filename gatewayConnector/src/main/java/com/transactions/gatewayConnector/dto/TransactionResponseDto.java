package com.transactions.gatewayConnector.dto;

import java.time.LocalDateTime;

public record TransactionResponseDto(
        String transactionId,
        double amount,
        TransactionStatus status,
        String remarks,
        LocalDateTime time
) {
}
