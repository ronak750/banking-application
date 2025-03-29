package com.transactions.transactions.schedulers;

import ch.qos.logback.classic.Level;
import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.StatusModel;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class NEFTRequestProcessingServiceTests {

    private static final String TEST_TRANSACTION_ID = "TXN123456";
    private static final String TEST_FROM_ACCOUNT = "1234567890";
    private static final String TEST_TO_ACCOUNT = "0987654321";
    private static final String TEST_FROM_IFSC = "BANK001";
    private static final String TEST_TO_IFSC = "BANK002";
    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1000.00);
    private static final String TEST_USER_ID = "USER001";

    @Mock
    private NEFTTransactionProcessingQueueRepo neftTransactionProcessingQueueRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private GatewayConnectorFeignClient gatewayConnectorFeignClient;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private NEFTRequestProcessingService neftRequestProcessingService;

    private List<NEFTProcessingTransaction> pendingRequests;
    private List<Transaction> existingTransactions;
    private GatewayConnectorTransactionListResponseDTO gatewayResponse;

    @BeforeEach
    void setup() {
        pendingRequests = createTestPendingRequests();
        existingTransactions = createTestTransactions();
        gatewayResponse = createTestGatewayResponse();
    }

    @Test
    void testProcessPendingRequestsWithEmptyList() {
        // Arrange
        when(neftTransactionProcessingQueueRepo.findAll()).thenReturn(new ArrayList<>());

        // Act
        neftRequestProcessingService.processPendingRequests();

        // Assert
        verify(neftTransactionProcessingQueueRepo).findAll();
        verifyNoMoreInteractions(gatewayConnectorFeignClient);
    }

    @Test
    void testProcessPendingRequestsSuccessfully() {
        // Arrange
        when(neftTransactionProcessingQueueRepo.findAll()).thenReturn(pendingRequests);
        when(gatewayConnectorFeignClient.transferNEFTMoney(anyList())).thenReturn(gatewayResponse);
        when(transactionRepo.findAllById(anyList())).thenReturn(existingTransactions);
        when(transactionRepo.saveAll(anyList())).thenReturn(existingTransactions);
        doNothing().when(neftTransactionProcessingQueueRepo).deleteAllInBatch(anyList());
        when(streamBridge.send(
                anyString(),
                any(MessageInfoDto.class)
        )).thenReturn(true);

        // Act
        neftRequestProcessingService.processPendingRequests();

        // Assert
        verify(neftTransactionProcessingQueueRepo).findAll();
        verify(gatewayConnectorFeignClient).transferNEFTMoney(anyList());
        verify(transactionRepo).findAllById(anyList());
        verify(transactionRepo).saveAll(anyList());
        verify(neftTransactionProcessingQueueRepo).deleteAllInBatch(pendingRequests);
        verify(streamBridge).send(eq("send-communication-out-0"), any(MessageInfoDto.class));
    }

//    @Test
//    void testCreateTransferRequestFromProcessingQueue() {
//        // Arrange
//        NEFTProcessingTransaction processingTransaction = pendingRequests.get(0);
//
//        // Act
//        NEFTtransferRequestDto requestDto = neftRequestProcessingService
//                .createTransferRequestFromTransactionProcessingQueueDoa(processingTransaction);
//
//        // Assert
//        assertNotNull(requestDto);
//        assertEquals(TEST_TRANSACTION_ID, requestDto.transactionId());
//        assertEquals(TEST_FROM_ACCOUNT, requestDto.fromAccountNumber());
//        assertEquals(TEST_TO_ACCOUNT, requestDto.toAccountNumber());
//        assertEquals(TEST_FROM_IFSC, requestDto.fromAccountIfsc());
//        assertEquals(TEST_TO_IFSC, requestDto.toAccountIfsc());
//        assertEquals(TEST_AMOUNT, requestDto.amount());
//    }

    // Helper methods to create test data
    private List<NEFTProcessingTransaction> createTestPendingRequests() {
        List<NEFTProcessingTransaction> requests = new ArrayList<>();
        NEFTProcessingTransaction transaction = new NEFTProcessingTransaction();
        transaction.setTransactionId(TEST_TRANSACTION_ID);
        transaction.setFromAccountNumber(TEST_FROM_ACCOUNT);
        transaction.setToAccountNumber(TEST_TO_ACCOUNT);
        transaction.setFromAccountIfsc(TEST_FROM_IFSC);
        transaction.setToAccountIfsc(TEST_TO_IFSC);
//        Level Bigecimal;
        transaction.setAmount(TEST_AMOUNT.doubleValue());
        transaction.setSubmittedBy(TEST_USER_ID);
        requests.add(transaction);
        return requests;
    }

    private List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setTransactionId(TEST_TRANSACTION_ID);
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transactions.add(transaction);
        return transactions;
    }

    private GatewayConnectorTransactionListResponseDTO createTestGatewayResponse() {
        return new GatewayConnectorTransactionListResponseDTO(
                new StatusModel(200, "Success"),
                "Fetched Successful",
                List.of(new TransactionResponseDTO(
                        TEST_TRANSACTION_ID,
                        TEST_AMOUNT.doubleValue(),
                        TEST_FROM_IFSC,
                        TEST_TO_IFSC,
                        TransactionStatus.SUCCESS,
                        "Transaction successful",
                        LocalDateTime.now()
                ))
        );
    }

}