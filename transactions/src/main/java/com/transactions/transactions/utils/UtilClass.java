package com.transactions.transactions.utils;

import com.transactions.transactions.dtos.MessageInfoDto;
import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.WalletTransactionResponseDTO;
import com.transactions.transactions.entities.Transaction;

import java.time.LocalDateTime;

public class UtilClass {

    public static WalletTransactionResponseDTO convertTransactionToWalletTransactionResponseDto(Transaction transaction) {
        return new WalletTransactionResponseDTO(
                   transaction.getTransactionId(),
                    transaction.getAmount(),
                    transaction.getSourceId(),
                    transaction.getDestinationId(),
                    transaction.getTransactionStatus(),
                    transaction.getDescription(),
                    transaction.getCreatedAt() == null ? LocalDateTime.now() : transaction.getCreatedAt()
                );
    }

    public static TransactionResponseDTO convertTransactionToTransactionResponseDto(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getSourceId(),
                transaction.getDestinationId(),
                transaction.getTransactionStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt() == null ? LocalDateTime.now() : transaction.getCreatedAt()
        );
    }

}
