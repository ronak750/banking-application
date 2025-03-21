package com.transactions.transactions.services;


import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.*;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.entities.TransactionType;
import com.transactions.transactions.exceptions.InvalidFieldException;
import com.transactions.transactions.exceptions.TransactionRequestFailedException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.UtilClass;
import lombok.AllArgsConstructor;
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



    public boolean validateUPIid(String upi) {
        return gatewayConnectorFeignClient.validateUPI(upi);
    }


    public TransactionResponseDTO transfer(UPITransferRequestDTO upiTransferRequestDTO, String userId) throws TransactionRequestFailedException {
        if(transactionRepo.findById(upiTransferRequestDTO.transactionId()).isPresent()) {
            throw new InvalidFieldException("Duplicate transfer request received");
        }

        if (upiTransferRequestDTO.amount() <= 0 || upiTransferRequestDTO.amount() > 1_00_000) {
            throw new InvalidFieldException("Invalid transfer amount");
        }

        if (upiTransferRequestDTO.fromUpiId().equals(upiTransferRequestDTO.toUpiId())) {
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

            throw new TransactionRequestFailedException(e.getMessage());
        }
    }

    public BigDecimal getBalance(UPIBalanceCheckRequestDto upiBalanceCheckRequestDto) throws TransactionRequestFailedException {
        try {
            return gatewayConnectorFeignClient.checkUPIBalance(upiBalanceCheckRequestDto );
        } catch (IllegalArgumentException e) {
            throw new TransactionRequestFailedException(e.getMessage());
        }
    }

    public List<TransactionResponseDTO> getTransactions(String upiId) {
        return transactionRepo
                .findBySourceIdOrDestinationId(upiId, upiId)
                .stream()
                .filter(transaction -> transaction.getTransactionType().equals(TransactionType.UPI))
                .map(UtilClass::convertTransactionToTransactionResponseDto)
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
