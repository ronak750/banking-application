package com.transactions.transactions.services;

import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.entity.NEFTProcessingTransaction;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.exception.InvalidFieldException;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
import lombok.AllArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.transactions.transactions.constant.Constants.*;
import static com.transactions.transactions.utils.UtilClass.convertTransactionToTransactionResponseDto;

@Service
@AllArgsConstructor
public class NEFTService {

    private final TransactionRepo transactionRepo;
    private final StreamBridge streamBridge;
    private final NEFTTransactionProcessingQueueRepo neftTransactionProcessingQueueRepo;

    private static final String REQUEST_ACCEPTED_FOR_NEFT_TRANSFER = "Transaction Request Accepted for NEFT Transfer";

    /**
     * Retrieves the transaction details by transaction id
     *
     * @param transactionId the transaction id to fetch details for
     * @return the transaction details
     * @throws InvalidFieldException if the transaction with the given id does not exist
     */
    public TransactionResponseDTO getTransactionDetailsByTransactionId(String transactionId) {

        Transaction transaction = transactionRepo.findById(transactionId).orElseThrow(
                () -> new InvalidFieldException("Transaction with id " + transactionId + " does not exist")
        );

        return convertTransactionToTransactionResponseDto(transaction);
    }

    public TransactionResponseDTO transferMoney(NEFTtransferRequestDto nefTtransferRequestDto, String userId) {

        checkValidations(nefTtransferRequestDto);

        Transaction transaction = createTransactionDoaFromTransferRequest(nefTtransferRequestDto);
        NEFTProcessingTransaction neftProcessingTransaction =
                createTransactionProcessingQueueDoaFromTransferRequest(nefTtransferRequestDto, userId);

        transactionRepo.save(transaction);
        neftTransactionProcessingQueueRepo.save(neftProcessingTransaction);
        sendTransactionNotification(
                "Transaction with id " + transaction.getTransactionId() + " has been accepted for NEFT transfer",
                userId
        );

        return TransactionResponseDTO.builder()
                .transactionId(nefTtransferRequestDto.transactionId())
                .status(TransactionStatus.PENDING)
                .amount(nefTtransferRequestDto.amount())
                .from(nefTtransferRequestDto.fromAccountNumber().concat("-").concat(nefTtransferRequestDto.fromIfscCode()))
                .to(nefTtransferRequestDto.toAccountNumber().concat("-").concat(nefTtransferRequestDto.toIfscCode()))
                .time(LocalDateTime.now())
                .remarks(REQUEST_ACCEPTED_FOR_NEFT_TRANSFER)
                .build();
    }


    private void sendTransactionNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
    }

    private Transaction createTransactionDoaFromTransferRequest(NEFTtransferRequestDto nefTtransferRequestDto) {
        return Transaction.builder()
                .transactionId(nefTtransferRequestDto.transactionId())
                .amount(nefTtransferRequestDto.amount())
                .sourceId(nefTtransferRequestDto.fromAccountNumber().concat("-").concat(nefTtransferRequestDto.fromIfscCode()))
                .destinationId(nefTtransferRequestDto.toAccountNumber().concat("-").concat(nefTtransferRequestDto.toIfscCode()))
                .transactionType(TransactionType.NEFT)
                .transactionStatus(TransactionStatus.PENDING)
                .description(REQUEST_ACCEPTED_FOR_NEFT_TRANSFER)
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

    private void checkValidations(NEFTtransferRequestDto request) {
        if(neftTransactionProcessingQueueRepo.findById(request.transactionId()).isPresent()) {
            throw new InvalidFieldException(DUPLICATE_TRANSACTION_ERROR_MSG);
        }

        if (request.amount() <= 0 || request.amount() > 1_00_000) {
            throw new InvalidFieldException(INVALID_AMOUNT_TRANSFER_ERROR_MSG);
        }

        if (request.fromAccountNumber().equals(request.toAccountNumber()) &&
            request.fromIfscCode().equals(request.toIfscCode())) {
            throw new InvalidFieldException(SAME_ACCOUNT_TRANSFER_ERROR_MSG);
        }
    }
}