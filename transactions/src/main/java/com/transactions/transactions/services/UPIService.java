package com.transactions.transactions.services;


import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.*;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.entities.TransactionType;
import com.transactions.transactions.exceptions.ApiException;
import com.transactions.transactions.exceptions.InvalidFieldException;
import com.transactions.transactions.exceptions.TransactionRequestFailedException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.UtilClass;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UPIService {

    private final TransactionRepo transactionRepo;
    private GatewayConnectorFeignClient gatewayConnectorFeignClient;
    private final StreamBridge streamBridge;

    private static final Logger log = LoggerFactory.getLogger(UPIService.class);

    public boolean validateUPIid(String upi) {
        return gatewayConnectorFeignClient.validateUPI(upi);
    }


    public TransactionResponseDTO transfer(UPITransferRequestDTO upiTransferRequestDTO, String userId) throws TransactionRequestFailedException {

        log.info("Initiating UPI transfer for transaction ID {}", upiTransferRequestDTO.transactionId());

        if(transactionRepo.findById(upiTransferRequestDTO.transactionId()).isPresent()) {
            log.info("Duplicate transfer request for transaction ID {}", upiTransferRequestDTO.transactionId());
            throw new InvalidFieldException("Duplicate transfer request received");
        }

        if (upiTransferRequestDTO.amount() <= 0 || upiTransferRequestDTO.amount() > 1_00_000) {
            log.info("Invalid transfer amount {} for transaction ID {}", upiTransferRequestDTO.amount(), upiTransferRequestDTO.transactionId());
            throw new InvalidFieldException("Invalid transfer amount");
        }

        if (upiTransferRequestDTO.fromUpiId().equals(upiTransferRequestDTO.toUpiId())) {
            log.info("Transfer attempt to the same upi for transaction ID {}", upiTransferRequestDTO.transactionId());
            throw new InvalidFieldException("Cannot transfer to the same account");
        }

        Transaction transaction = createTransactionDoaFromTransferRequest(upiTransferRequestDTO);

        transactionRepo.save(transaction);

        try{

            var gcTransactionResponseDto = gatewayConnectorFeignClient
                    .transferUpiMoney(upiTransferRequestDTO);

            transaction.setTransactionStatus(gcTransactionResponseDto.getStatus());
            transaction.setDescription(gcTransactionResponseDto.getRemarks());
            transactionRepo.save(transaction);

            gcTransactionResponseDto.setFrom(upiTransferRequestDTO.fromUpiId());
            gcTransactionResponseDto.setTo(upiTransferRequestDTO.toUpiId());
            sendTransactionNotification(
                    "Transaction with id " + transaction.getTransactionId() + " has been " + gcTransactionResponseDto.getStatus(),
                    userId
            );
            log.info("UPI Transaction with id {} is successful", transaction.getTransactionId());

            return gcTransactionResponseDto;

        } catch (Exception e) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction.setDescription(e.getMessage());
            transactionRepo.save(transaction);
            sendTransactionNotification(
                    "Transaction with id " + transaction.getTransactionId() + " has failed.",
                    userId
            );
            log.info("UPI Transaction with id {} has failed with error {}", transaction.getTransactionId(), e.getMessage());

            throw new TransactionRequestFailedException(e.getMessage());
        }
    }

    /**
     * Gets the balance of a user's UPI account.
     *
     * @param upiBalanceCheckRequestDto The UPI account details and PIN.
     * @return The balance of the UPI account.
     * @throws TransactionRequestFailedException if the request fails.
     */
    public BigDecimal getBalance(UPIBalanceCheckRequestDto upiBalanceCheckRequestDto) throws TransactionRequestFailedException {
        try {
            return gatewayConnectorFeignClient.checkUPIBalance(upiBalanceCheckRequestDto );
        } catch (ApiException ex) {
            System.out.println("");
            throw ex;
        }
    }

    /**
     * Retrieves the list of transactions for a given UPI ID.
     *
     * @param upiId The UPI ID.
     * @return The list of transactions.
     */
    public List<TransactionResponseDTO> getTransactions(String upiId) {
        log.info("Retrieving transactions for UPI ID: {}", upiId);

        return transactionRepo
                .findBySourceIdOrDestinationId(upiId, upiId)
                .stream()
                .filter(transaction -> transaction.getTransactionType().equals(TransactionType.UPI))
                .map(UtilClass::convertTransactionToTransactionResponseDto)
                .peek(transaction -> log.info("Transaction found: {}", transaction))
                .toList();
    }

    private void sendTransactionNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
    }

    private Transaction createTransactionDoaFromTransferRequest(UPITransferRequestDTO transferRequest) {
        return Transaction.builder()
                .transactionId(transferRequest.transactionId())
                .amount(transferRequest.amount())
                .sourceId(transferRequest.fromUpiId())
                .destinationId(transferRequest.toUpiId())
                .transactionType(TransactionType.UPI)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
    }
}
