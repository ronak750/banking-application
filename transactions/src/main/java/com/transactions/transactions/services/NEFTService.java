package com.transactions.transactions.services;

import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.TransactionStatus;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.entities.NEFTProcessingTransaction;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.entities.TransactionType;
import com.transactions.transactions.exceptions.InvalidFieldException;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class NEFTService {

    private final TransactionRepo transactionRepo;

    private final NEFTTransactionProcessingQueueRepo neftTransactionProcessingQueueRepo;

    public TransactionResponseDTO transferMoney(NEFTtransferRequestDto nefTtransferRequestDto, String userId) throws Exception {

        if(neftTransactionProcessingQueueRepo.findById(nefTtransferRequestDto.transactionId()).isPresent()) {
            throw new InvalidFieldException("Duplicate transfer request received");
        }

        if (nefTtransferRequestDto.amount() <= 0 || nefTtransferRequestDto.amount() > 1_00_000) {
            throw new InvalidFieldException("Invalid transfer amount");
        }

        if (nefTtransferRequestDto.fromAccountNumber().equals(nefTtransferRequestDto.toAccountNumber()) &&
            nefTtransferRequestDto.fromIfscCode().equals(nefTtransferRequestDto.toIfscCode())) {
            throw new InvalidFieldException("Cannot transfer to the same wallet");
        }

        Transaction transaction = createTransactionDoaFromTransferRequest(nefTtransferRequestDto);
        NEFTProcessingTransaction neftProcessingTransaction =
                createTransactionProcessingQueueDoaFromTransferRequest(nefTtransferRequestDto, userId);

        transactionRepo.save(transaction);
        neftTransactionProcessingQueueRepo.save(neftProcessingTransaction);

        return TransactionResponseDTO.builder()
                .transactionId(nefTtransferRequestDto.transactionId())
                .status(TransactionStatus.PENDING)
                .amount(nefTtransferRequestDto.amount())
                .from(nefTtransferRequestDto.fromAccountNumber().concat("-").concat(nefTtransferRequestDto.fromIfscCode()))
                .to(nefTtransferRequestDto.toAccountNumber().concat("-").concat(nefTtransferRequestDto.toIfscCode()))
                .time(LocalDateTime.now())
                .build();
    }

    private Transaction createTransactionDoaFromTransferRequest(NEFTtransferRequestDto nefTtransferRequestDto) {
        return Transaction.builder()
                .transactionId(nefTtransferRequestDto.transactionId())
                .amount(nefTtransferRequestDto.amount())
                .sourceId(nefTtransferRequestDto.fromAccountNumber().concat("-").concat(nefTtransferRequestDto.fromIfscCode()))
                .destinationId(nefTtransferRequestDto.toAccountNumber().concat("-").concat(nefTtransferRequestDto.toIfscCode()))
                .transactionType(TransactionType.NEFT)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
    }

    private NEFTProcessingTransaction createTransactionProcessingQueueDoaFromTransferRequest(
            NEFTtransferRequestDto nefTtransferRequestDto,
            String userId
    ) {
        return new NEFTProcessingTransaction(
                    nefTtransferRequestDto.transactionId(),
                    nefTtransferRequestDto.fromAccountNumber(),
                    nefTtransferRequestDto.toAccountNumber(),
                    nefTtransferRequestDto.fromIfscCode(),
                    nefTtransferRequestDto.toIfscCode(),
                    nefTtransferRequestDto.amount(),
                    LocalDateTime.now(),
                    userId
                );
    }

}