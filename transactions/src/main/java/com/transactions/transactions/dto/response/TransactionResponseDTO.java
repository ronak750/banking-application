package com.transactions.transactions.dto.response;

import com.transactions.transactions.dto.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class TransactionResponseDTO {
    String transactionId;
    double amount;
    String from;
    String to;
    TransactionStatus status;
    String remarks;
    LocalDateTime time;
}
