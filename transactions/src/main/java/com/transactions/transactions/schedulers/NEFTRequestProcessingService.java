package com.transactions.transactions.schedulers;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.MessageInfoDto;
import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.entities.NEFTProcessingTransaction;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
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

    @Scheduled(fixedRate = 120000) // 2 minutes = 180000 ms
    @Transactional
    public void processPendingRequests() {
        List<NEFTProcessingTransaction> pendingRequests = neftTransactionProcessingQueueRepo.findAll();
        log.info("found {} pending NEFT requests", pendingRequests.size());

        processRequests(pendingRequests);
    }

    @Transactional
    public void processRequests(List<NEFTProcessingTransaction> pendingRequests) {

        var listOfTransactionResponseDto = gatewayConnectorFeignClient.transferNEFTMoney(
                pendingRequests
                        .stream()
                        .map(this::createTransferRequestFromTransactionProcessingQueueDoa)
                        .toList()
        );

        Map<String, TransactionResponseDTO> transactionResponseDTOMap = listOfTransactionResponseDto.stream()
                .collect(Collectors.toMap(TransactionResponseDTO::getTransactionId, Function.identity()));

        Map<String, NEFTProcessingTransaction> pendingRequestMap = pendingRequests.stream()
                .collect(Collectors.toMap(NEFTProcessingTransaction::getTransactionId, Function.identity()));

        var transactionsList = transactionRepo.findAllById(listOfTransactionResponseDto.stream().map(TransactionResponseDTO::getTransactionId).toList());

        var updatedTransaction = transactionsList
                .stream()
                .peek(transaction -> {
                    sendTransactionNotification(
                            "NEFT transaction with transaction id " +
                                    transaction.getTransactionId() + " is " +
                                    transactionResponseDTOMap.get(transaction.getTransactionId()).getStatus(),
                            pendingRequestMap.get(transaction.getTransactionId()).getSubmittedBy()
                    );
                    transaction.setTransactionStatus(transactionResponseDTOMap.get(transaction.getTransactionId()).getStatus());
                    transaction.setDescription(transactionResponseDTOMap.get(transaction.getTransactionId()).getRemarks());
                }).toList();


        transactionRepo.saveAll(updatedTransaction);

        neftTransactionProcessingQueueRepo.deleteAllInBatch(pendingRequests);
    }


    private void sendTransactionNotification(String message, String userId) {
        streamBridge.send("send-communication-out-0", new MessageInfoDto(userId, message));
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
