package com.transactions.transactions.entity;

import com.transactions.transactions.dto.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction extends BaseEntity{
    @Id
    String transactionId;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    String sourceId;

    String destinationId;

    double amount;

    @Enumerated(EnumType.STRING)
    TransactionStatus transactionStatus;

    String description;
}
