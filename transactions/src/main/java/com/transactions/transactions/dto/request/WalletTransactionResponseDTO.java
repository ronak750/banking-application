package com.transactions.transactions.dto.request;

import com.transactions.transactions.dto.TransactionStatus;

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