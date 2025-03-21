package com.transactions.transactions.services;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.TransactionStatus;
import com.transactions.transactions.dtos.WalletTransactionResponseDTO;
import com.transactions.transactions.dtos.WalletTransferRequestDto;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.entities.TransactionType;
import com.transactions.transactions.exceptions.InvalidFieldException;
import com.transactions.transactions.exceptions.TransactionRequestFailedException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.UtilClass;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.transactions.transactions.utils.UtilClass.convertTransactionToWalletTransactionResponseDto;


@Service
@AllArgsConstructor
public class WalletService {

    private GatewayConnectorFeignClient gatewayConnectorFeignClient;

    private final TransactionRepo transactionRepo;

    public BigDecimal getBalance(Long walletId) throws TransactionRequestFailedException {
        if(walletId <= 0) {
            throw new InvalidFieldException("Invalid wallet id provided");
        }
        try {
            return gatewayConnectorFeignClient.checkWalletBalance(walletId.toString());
        } catch (Exception e) {
            throw new TransactionRequestFailedException(e.getMessage());
        }

    }

    public WalletTransactionResponseDTO transferMoney(WalletTransferRequestDto transferRequest) throws Exception {

        if(transactionRepo.findById(transferRequest.transactionId()).isPresent()) {
            throw new InvalidFieldException("Duplicate transfer request received");
        }

        if (transferRequest.amount() <= 0 || transferRequest.amount() > 1_00_000) {
            throw new InvalidFieldException("Invalid transfer amount");
        }

        if (transferRequest.fromWalletId().equals(transferRequest.toWalletId())) {
            throw new InvalidFieldException("Cannot transfer to the same wallet");
        }

        Transaction transaction = createTransactionDoaFromTransferRequest(transferRequest);

        transactionRepo.save(transaction);

        try{

            TransactionResponseDTO gcTransactionResponseDto = gatewayConnectorFeignClient
                    .transferWalletMoney(transferRequest);

            transaction.setTransactionStatus(gcTransactionResponseDto.getStatus());
            transaction.setDescription(gcTransactionResponseDto.getRemarks());
            transactionRepo.save(transaction);

            return convertTransactionToWalletTransactionResponseDto(transaction);

        } catch (Exception e) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setDescription(e.getMessage());
            transactionRepo.save(transaction);
            throw new TransactionRequestFailedException(e.getMessage());
        }
    }

    public List<WalletTransactionResponseDTO> getTransactions(Long walletId) {
        return transactionRepo
                .findBySourceIdOrDestinationId(walletId.toString(), walletId.toString())
                .stream()
                .filter(transaction -> transaction.getTransactionType().equals(TransactionType.Wallet))
                .map(UtilClass::convertTransactionToWalletTransactionResponseDto)
                .toList();
    }

    private Transaction createTransactionDoaFromTransferRequest(WalletTransferRequestDto transferRequest) {
        return Transaction.builder()
                .transactionId(transferRequest.transactionId())
                .amount(transferRequest.amount())
                .sourceId(transferRequest.fromWalletId().toString())
                .destinationId(transferRequest.toWalletId().toString())
                .transactionType(TransactionType.Wallet)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
    }

}