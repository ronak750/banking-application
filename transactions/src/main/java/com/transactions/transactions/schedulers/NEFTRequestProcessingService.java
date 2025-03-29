package com.transactions.transactions.schedulers;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.response.GatewayConnectorTransactionListResponseDTO;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.entity.NEFTProcessingTransaction;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.MessageTemplates;
import com.transactions.transactions.utils.UtilClass;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NEFTRequestProcessingService {

    private final NEFTTransactionProcessingQueueRepo neftTransactionProcessingQueueRepo;
    private final TransactionRepo transactionRepo;
    private GatewayConnectorFeignClient gatewayConnectorFeignClient;
    private final StreamBridge streamBridge;


    private static final Logger log = LoggerFactory.getLogger(NEFTRequestProcessingService.class);

    @Scheduled(fixedRate = 120000) // 2 minutes = 120000 ms
    @Transactional
    public void processPendingRequests() {
        List<NEFTProcessingTransaction> pendingRequests = neftTransactionProcessingQueueRepo.findAll();
        log.info("found {} pending NEFT requests", pendingRequests.size());

        if(!pendingRequests.isEmpty())
            processRequests(pendingRequests);
    }

    @Transactional
    public void processRequests(List<NEFTProcessingTransaction> pendingRequests) {

        GatewayConnectorTransactionListResponseDTO listOfGatewayTransactionResponseDto = gatewayConnectorFeignClient.transferNEFTMoney(
                pendingRequests
                        .stream()
                        .map(this::createTransferRequestFromTransactionProcessingQueueDoa)
                        .toList()
        );

        var listOfTransactionResponseDto = UtilClass.getListOfTransactionsFromGatewayConnectorResponse(listOfGatewayTransactionResponseDto);

        Map<String, TransactionResponseDTO> transactionResponseDTOMap = listOfTransactionResponseDto.stream()
                .collect(Collectors.toMap(TransactionResponseDTO::getTransactionId, Function.identity()));

        Map<String, NEFTProcessingTransaction> pendingRequestMap = pendingRequests.stream()
                .collect(Collectors.toMap(NEFTProcessingTransaction::getTransactionId, Function.identity()));

        var transactionsList = transactionRepo.findAllById(listOfTransactionResponseDto.stream().map(TransactionResponseDTO::getTransactionId).toList());

        var updatedTransaction = getTransactionsUpdated(
                transactionsList,
                transactionResponseDTOMap,
                pendingRequestMap
        );

        transactionRepo.saveAll(updatedTransaction);

        neftTransactionProcessingQueueRepo.deleteAllInBatch(pendingRequests);
    }


    private void sendTransactionNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
    }


    private List<Transaction> getTransactionsUpdated(
            List<Transaction> transactionsList,
            Map<String, TransactionResponseDTO> transactionResponseDTOMap,
            Map<String, NEFTProcessingTransaction> pendingRequestMap
    ) {
        return transactionsList.stream()
                .map(transaction -> {

                    String transactionId = transaction.getTransactionId();
                    TransactionResponseDTO responseDTO = transactionResponseDTOMap.get(transactionId);
                    var pendingRequest = pendingRequestMap.get(transactionId);

                    // Send notification
                    sendTransactionNotification(
                            MessageTemplates.prepareTransactionMessage(
                                    TransactionType.NEFT,
                                    transactionId,
                                    responseDTO.getStatus()
                            ),
                            pendingRequest.getSubmittedBy()
                    );

                    // Set updated fields
                    transaction.setTransactionStatus(responseDTO.getStatus());
                    transaction.setDescription(responseDTO.getRemarks());

                    return transaction;
                }).toList();
    }

    private NEFTtransferRequestDto createTransferRequestFromTransactionProcessingQueueDoa(
            NEFTProcessingTransaction neftProcessingTransaction
    ) {
        return new NEFTtransferRequestDto(
                neftProcessingTransaction.getTransactionId(),
                neftProcessingTransaction.getFromAccountNumber(),
                neftProcessingTransaction.getToAccountNumber(),
                neftProcessingTransaction.getFromAccountIfsc(),
                neftProcessingTransaction.getToAccountIfsc(),
                neftProcessingTransaction.getAmount()
        );
    }
}
