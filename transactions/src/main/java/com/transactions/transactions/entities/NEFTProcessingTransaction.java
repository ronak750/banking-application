package com.transactions.transactions.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "neft_processing_queue")
public class NEFTProcessingTransaction {

    @Id
    String transactionId;
    String fromAccountNumber;
    String fromAccountIfsc;
    String toAccountNumber;
    String toAccountIfsc;
    double amount;
    LocalDateTime scheduledAt;
    String submittedBy;
}
