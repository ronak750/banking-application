package com.transactions.transactions.services;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.dto.response.ValidationResponseDTO;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.exception.ApiException;
import com.transactions.transactions.exception.InvalidFieldException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.MessageTemplates;
import com.transactions.transactions.utils.UtilClass;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Pageable;
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

    public ValidationResponseDTO validateUPIid(String upi) {
        var validationGatewayResponse = gatewayConnectorFeignClient.validateUPI(upi);
        var validationResponseDTO = UtilClass.getListOfTransactionsFromGatewayConnectorResponse(validationGatewayResponse);
        return new ValidationResponseDTO(validationResponseDTO.getIsValid());
    }

    /**
     * Validate the transfer request and then transfers money from one UPI account to another.
     *
     * @param upiTransferRequestDTO The UPI account details and PIN and other information about the transfer of money.
     * @param userId The userId who is making the transfer.
     * @return The details including the status and remarks of the transaction.
     * @throws ApiException if the request fails.
     */
    public TransactionResponseDTO transfer(UPITransferRequestDTO upiTransferRequestDTO, String userId) {

        log.info("Initiating UPI transfer for transaction ID {}", upiTransferRequestDTO.transactionId());

        checkTransferRequestData(upiTransferRequestDTO);

        Transaction transaction = createTransactionDoaFromTransferRequest(upiTransferRequestDTO);

        transactionRepo.save(transaction);

        try{

            var gcTransactionResponseDto = gatewayConnectorFeignClient
                    .transferUpiMoney(upiTransferRequestDTO);
            var transactionResponseDTO = UtilClass.fetchTransactionFromGatewayConnectorResponseDTO(gcTransactionResponseDto);

            transaction.setTransactionStatus(transactionResponseDTO.getStatus());
            transaction.setDescription(transactionResponseDTO.getRemarks());
            transactionRepo.save(transaction);

            transactionResponseDTO.setFrom(upiTransferRequestDTO.fromUpiId());
            transactionResponseDTO.setTo(upiTransferRequestDTO.toUpiId());
            sendTransactionNotification(
                    MessageTemplates.prepareTransactionMessage(TransactionType.UPI, transaction.getTransactionId(), transactionResponseDTO.getStatus()),
                    userId
            );
            log.info("UPI Transaction with id {} is successful", transaction.getTransactionId());
            return transactionResponseDTO;

        } catch (ApiException e) {
            log.info("UPI Transaction with id {} has failed", transaction.getTransactionId());
            handleUpiTransactionFailure(transaction, e.getMessage(), userId);
            throw e;
        }
    }


    /**
     * Gets the balance of a user's UPI account.
     *
     * @param upiBalanceCheckRequestDto The UPI account details and PIN.
     * @return The balance of the UPI account.
     * @throws ApiException if the request fails.
     */
    public BigDecimal getBalance(UPIBalanceCheckRequestDto upiBalanceCheckRequestDto) {
        log.info("Fetching UPI balance for {}", upiBalanceCheckRequestDto.upiId());
        try {
            var balanceResponseDto = gatewayConnectorFeignClient.checkUPIBalance(upiBalanceCheckRequestDto);
            BigDecimal balance = UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(balanceResponseDto);
            log.info("UPI balance successfully fetched for {}", upiBalanceCheckRequestDto.upiId());
            return balance;
        } catch (ApiException ex) {
            log.info("Failed to fetch UPI balance for UPI id {}", upiBalanceCheckRequestDto.upiId());
            throw ex;
        }
    }

    /**
     * Retrieves the list of transactions for a given UPI ID.
     *
     * @param upiId The UPI ID.
     * @return The list of transactions.
     */
    public List<TransactionResponseDTO> getTransactions(String upiId, Pageable pageable) {
        log.info("Retrieving transactions for UPI ID: {}", upiId);
        return transactionRepo.findBySourceIdOrDestinationIdAndTransactionType(
                        upiId,
                        upiId,
                        TransactionType.UPI,
                        pageable
                ).stream()
                    .map(UtilClass::convertTransactionToTransactionResponseDto)
                    .toList();

    }

    private void sendTransactionNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
    }


    private void handleUpiTransactionFailure(Transaction transaction, String message, String userId) {
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setDescription(message);
        transactionRepo.save(transaction);
        sendTransactionNotification(
                MessageTemplates.prepareTransactionMessage(
                        TransactionType.UPI,
                        transaction.getTransactionId(),
                        TransactionStatus.FAILED
                ),
                userId
        );
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

    private void checkTransferRequestData(UPITransferRequestDTO upiTransferRequestDTO){
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

    }
}
