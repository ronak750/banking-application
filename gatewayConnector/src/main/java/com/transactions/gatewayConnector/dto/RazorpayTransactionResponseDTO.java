package com.transactions.gatewayconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
@Getter
public class RazorpayTransactionResponseDTO {
    Boolean isSuccess;
    String message;
    String transactionId;
    double amount;
    LocalDateTime time;
}
