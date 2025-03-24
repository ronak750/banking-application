package com.transactions.transactions.services;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.*;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.entities.TransactionType;
import com.transactions.transactions.exceptions.ApiException;
import com.transactions.transactions.exceptions.InvalidFieldException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.UtilClass;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.transactions.transactions.utils.UtilClass.convertTransactionToWalletTransactionResponseDto;


@Service
@AllArgsConstructor
public class WalletService {

    private GatewayConnectorFeignClient gatewayConnectorFeignClient;
    private final StreamBridge streamBridge;
    private final TransactionRepo transactionRepo;

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    /**
     * Fetches the balance for a given wallet
     * @param walletId The id of the wallet to fetch the balance for
     * @return The balance for the given wallet
     * @throws ApiException If the request to the connector fails
     */
    public BigDecimal getBalance(Long walletId) throws ApiException {
        log.info("Fetching wallet balance for {}", walletId);
        if(walletId <= 0) {
            log.info("Invalid wallet id {} provided", walletId);
            throw new InvalidFieldException("Invalid wallet id provided");
        }
        try {
            BigDecimal balance = gatewayConnectorFeignClient.checkWalletBalance(walletId.toString());
            log.info("Wallet balance successfully fetched for {}", walletId);
            return balance;
        } catch (ApiException ex) {
            log.info("Failed to fetch wallet balance for wallet id {} with error {}", walletId, ex.getMessage());
            throw ex;
        }
    }


    /**
     * Transfers money between wallets based on the transfer request
     * @param transferRequest The transfer request containing transaction details
     * @param userId The ID of the user initiating the transfer
     * @return Response containing details of the wallet transaction
     * @throws ApiException If the transfer operation fails
     */
    public WalletTransactionResponseDTO transferMoney(WalletTransferRequestDto transferRequest, String userId) throws ApiException {

        log.info("Initiating wallet transfer for transaction ID {}", transferRequest.transactionId());

        if(transactionRepo.findById(transferRequest.transactionId()).isPresent()) {
            log.info("Duplicate transfer request for transaction ID {}", transferRequest.transactionId());
            throw new InvalidFieldException("Duplicate transfer request received");
        }

        if (transferRequest.amount() <= 0 || transferRequest.amount() > 1_00_000) {
            log.info("Invalid transfer amount {} for transaction ID {}", transferRequest.amount(), transferRequest.transactionId());
            throw new InvalidFieldException("Invalid transfer amount");
        }

        if (transferRequest.fromWalletId().equals(transferRequest.toWalletId())) {
            log.info("Transfer attempt to the same wallet for transaction ID {}", transferRequest.transactionId());
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
            sendNotification(
                    "Transaction with id " + transaction.getTransactionId() + " has been " + gcTransactionResponseDto.getStatus(),
                    userId
            );
            log.info("Transaction with id {} is successful", transaction.getTransactionId());

            return convertTransactionToWalletTransactionResponseDto(transaction);
        } catch (ApiException e) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setDescription(e.getMessage());
            transactionRepo.save(transaction);
            sendNotification(
                    "Transaction with id " + transaction.getTransactionId() + " has failed.",
                    userId
            );
            log.info("Transaction with id {} has failed with error {}", transaction.getTransactionId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a list of wallet transactions for the given wallet id
     * @param walletId The wallet id to fetch transactions for
     * @return A list of wallet transactions
     */
    public List<WalletTransactionResponseDTO> getTransactions(Long walletId) {
        log.info("Fetching wallet transactions for {}", walletId);
        return transactionRepo
                .findBySourceIdOrDestinationId(walletId.toString(), walletId.toString())
                .stream()
                .filter(transaction -> transaction.getTransactionType().equals(TransactionType.Wallet))
                .map(UtilClass::convertTransactionToWalletTransactionResponseDto)
                .toList();
    }

    private void sendNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
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