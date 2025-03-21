package com.transactions.transactions.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

public record WalletTransactionResponseDTO(
        String transactionId,
        double amount,
        String fromWalletId,
        String toWalletId,
        TransactionStatus status,
        String remarks,
        LocalDateTime time) {
}