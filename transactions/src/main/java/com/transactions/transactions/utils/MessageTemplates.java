package com.transactions.transactions.utils;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.entity.TransactionType;

public class MessageTemplates {

    public static String prepareTransactionMessage(
            TransactionType transactionType,
            String transactionId,
            TransactionStatus status
    ) {
        return transactionType.name() + " transaction with id " + transactionId + " has been " + status.name();
    }
}
