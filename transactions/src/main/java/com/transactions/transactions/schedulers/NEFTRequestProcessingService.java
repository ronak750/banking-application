package com.transactions.transactions.schedulers;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.entities.NEFTProcessingTransaction;
import com.transactions.transactions.entities.Transaction;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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

    @Scheduled(fixedRate = 120000) // 2 minutes = 180000 ms
    @Transactional
    public void processPendingRequests() {
        List<NEFTProcessingTransaction> pendingRequests = neftTransactionProcessingQueueRepo.findAll();
        System.out.println("found " + pendingRequests.size() + " pending requests");

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

        var transactionsList = transactionRepo.findAllById(listOfTransactionResponseDto.stream().map(TransactionResponseDTO::getTransactionId).toList());

        var updatedTransaction = transactionsList
                .stream()
                .peek(transaction -> {
                    transaction.setTransactionStatus(transactionResponseDTOMap.get(transaction.getTransactionId()).getStatus());
                    transaction.setDescription(transactionResponseDTOMap.get(transaction.getTransactionId()).getRemarks());
                }).toList();


        transactionRepo.saveAll(updatedTransaction);

        neftTransactionProcessingQueueRepo.deleteAllInBatch(pendingRequests);
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
