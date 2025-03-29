package com.transactions.transactions.services;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.request.WalletTransactionResponseDTO;
import com.transactions.transactions.dto.request.WalletTransferRequestDto;
import com.transactions.transactions.dto.response.BalanceResponseDTO;
import com.transactions.transactions.dto.response.GatewayConnectorBalanceResponseDTO;
import com.transactions.transactions.dto.response.GatewayConnectorTransactionResponseDTO;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.exception.ApiException;
import com.transactions.transactions.exception.InvalidFieldException;
import com.transactions.transactions.repos.TransactionRepo;
import com.transactions.transactions.utils.UtilClass;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.transactions.transactions.constant.Constants.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    // Constants for test data
    private static final Long VALID_WALLET_ID = 12345L;
    private static final Long INVALID_WALLET_ID = -1L;
    private static final BigDecimal EXPECTED_BALANCE = BigDecimal.valueOf(1000.50);
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final String SUCCESS_STATUS_MSG = "Success";
    private static final String RESPONSE_MESSAGE = "Balance retrieved successfully";
    // Constants for test data
    private static final String TEST_WALLET_ID_STRING = "12345";
    private static final String TEST_TRANSACTION_ID_1 = "TXNWALLET001";
    private static final String TEST_TRANSACTION_ID_2 = "TXNWALLET002";
    private static final double TEST_AMOUNT_1 = 100.50;
    private static final double TEST_AMOUNT_2 = 200.75;
    private static final String TEST_SOURCE_ID_1 = "123456";
    private static final String TEST_DESTINATION_ID_1 = "67890";
    private static final TransactionType TEST_TRANSACTION_TYPE = TransactionType.Wallet;


    @Mock
    private GatewayConnectorFeignClient gatewayConnectorFeignClient;
    @Mock
    private TransactionRepo transactionRepo;
    @Mock
    private StreamBridge streamBridge;


    @InjectMocks
    private WalletService walletService;

    private WalletTransferRequestDto validTransferRequest;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        validTransferRequest = new WalletTransferRequestDto(
                "TXN001",
                1L,
                2L,
                500.00
        );

        // Ensure no duplicate transaction exists

    }


    @Test
    void testGetBalanceSuccessful() {
        // Arrange
        GatewayConnectorBalanceResponseDTO mockResponse = new GatewayConnectorBalanceResponseDTO(
                new StatusModel(SUCCESS_STATUS_CODE, SUCCESS_STATUS_MSG),
                RESPONSE_MESSAGE,
                new BalanceResponseDTO(EXPECTED_BALANCE)
        );

        when(gatewayConnectorFeignClient.checkWalletBalance(VALID_WALLET_ID.toString()))
                .thenReturn(mockResponse);

        // Act
        BigDecimal balance = walletService.getBalance(VALID_WALLET_ID);

        // Assert
        assertNotNull(balance);
        assertEquals(EXPECTED_BALANCE, balance);

        // Verify interactions
        verify(gatewayConnectorFeignClient).checkWalletBalance(VALID_WALLET_ID.toString());
    }

    @Test
    void testGetBalanceWithInvalidWalletId() {
        // Act & Assert
        InvalidFieldException exception = assertThrows(
                InvalidFieldException.class,
                () -> walletService.getBalance(INVALID_WALLET_ID)
        );

        assertEquals("Invalid wallet id" + INVALID_WALLET_ID + " provided", exception.getMessage());

        // Verify no interactions with feign client
        verifyNoInteractions(gatewayConnectorFeignClient);
    }

    @Test
    void testGetBalanceWithApiException() {
        // Arrange
        ApiException mockApiException = new ApiException(503, "Gateway connection failed", INTERNAL_SERVER_ERROR);

        when(gatewayConnectorFeignClient.checkWalletBalance(VALID_WALLET_ID.toString()))
                .thenThrow(mockApiException);

        // Act & Assert
        ApiException thrownException = assertThrows(
                ApiException.class,
                () -> walletService.getBalance(VALID_WALLET_ID)
        );

        assertEquals("Gateway connection failed", thrownException.getErrorMessage());

        // Verify interactions
        verify(gatewayConnectorFeignClient).checkWalletBalance(VALID_WALLET_ID.toString());
    }

    @Test
    void testFetchBalanceFromResponseWithValidResponse() {
        // Arrange
        GatewayConnectorBalanceResponseDTO mockResponse = new GatewayConnectorBalanceResponseDTO(
                new StatusModel(SUCCESS_STATUS_CODE, SUCCESS_STATUS_MSG),
                RESPONSE_MESSAGE,
                new BalanceResponseDTO(EXPECTED_BALANCE)
        );

        // Act
        BigDecimal balance = UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(mockResponse);

        // Assert
        assertEquals(EXPECTED_BALANCE, balance);
    }

    @Test
    void testFetchBalanceFromResponseWithValidResponseFailedDueToNoProperResponseFromFeignClient() {
        // Arrange
        GatewayConnectorBalanceResponseDTO mockResponse = new GatewayConnectorBalanceResponseDTO(
                new StatusModel(SUCCESS_STATUS_CODE, SUCCESS_STATUS_MSG),
                null,
               null
        );


        when(gatewayConnectorFeignClient.checkWalletBalance(VALID_WALLET_ID.toString()))
                .thenReturn(mockResponse);

        // Act & Assert
        assertThrows(
                ApiException.class,
                () -> walletService.getBalance(VALID_WALLET_ID)
        );
    }


    @Test
    void testGetTransactionsWithResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Create mock transactions
        Transaction transaction1 = new Transaction(
                TEST_TRANSACTION_ID_1,
                TransactionType.Wallet,
                TEST_SOURCE_ID_1,
                TEST_DESTINATION_ID_1,
                TEST_AMOUNT_1,
                TransactionStatus.SUCCESS,
                SUCCESS_STATUS_MSG
        );

        Transaction transaction2 = new Transaction(
                TEST_TRANSACTION_ID_2,
                TransactionType.Wallet,
                TEST_SOURCE_ID_1,
                TEST_DESTINATION_ID_1,
                TEST_AMOUNT_2,
                TransactionStatus.FAILED,
                "Failed"
        );
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(transaction1, transaction2), PageRequest.of(0, 20), 0);

        // Mock repository method
        when(transactionRepo.findBySourceIdOrDestinationIdAndTransactionType(
                TEST_WALLET_ID_STRING,
                TEST_WALLET_ID_STRING,
                TEST_TRANSACTION_TYPE,
                pageable
        )).thenReturn(transactionPage);

        // Act
        List<WalletTransactionResponseDTO> transactions = walletService.getTransactions(TEST_WALLET_ID_STRING, pageable);

        // Assert
        assertNotNull(transactions);
        assertEquals(2, transactions.size());

        // Verify first transaction details
        WalletTransactionResponseDTO responseDto1 = transactions.get(0);
        assertEquals(TEST_TRANSACTION_ID_1, responseDto1.transactionId());
        assertEquals(TEST_AMOUNT_1, responseDto1.amount());
        assertEquals(TEST_SOURCE_ID_1, responseDto1.fromWalletId());
        assertEquals(TEST_DESTINATION_ID_1, responseDto1.toWalletId());
        assertEquals(TransactionStatus.SUCCESS, responseDto1.status());

        // Verify second transaction details
        WalletTransactionResponseDTO responseDto2 = transactions.get(1);
        assertEquals(TEST_TRANSACTION_ID_2, responseDto2.transactionId());
        assertEquals(TEST_AMOUNT_2, responseDto2.amount());
        assertEquals(TEST_DESTINATION_ID_1, responseDto2.toWalletId());
        assertEquals(TEST_SOURCE_ID_1, responseDto2.fromWalletId());
        assertEquals(TransactionStatus.FAILED, responseDto2.status());

        // Verify repository method was called
        verify(transactionRepo).findBySourceIdOrDestinationIdAndTransactionType(
                TEST_WALLET_ID_STRING,
                TEST_WALLET_ID_STRING,
                TEST_TRANSACTION_TYPE,
                pageable
        );
    }

    @Test
    void testGetTransactionsWithNoResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);


        // Mock repository method to return empty list
        when(transactionRepo.findBySourceIdOrDestinationIdAndTransactionType(
                TEST_WALLET_ID_STRING,
                TEST_WALLET_ID_STRING,
                TEST_TRANSACTION_TYPE,
                pageable
        )).thenReturn(transactionPage);

        // Act
        List<WalletTransactionResponseDTO> transactions = walletService.getTransactions(TEST_WALLET_ID_STRING, pageable);

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        // Verify repository method was called
        verify(transactionRepo).findBySourceIdOrDestinationIdAndTransactionType(
                TEST_WALLET_ID_STRING,
                TEST_WALLET_ID_STRING,
                TEST_TRANSACTION_TYPE,
                pageable
        );
    }



    @Test
    void transferMoneySuccessfulTransferReturnsCorrectResponse() {
        // Prepare mock gateway connector response
        GatewayConnectorTransactionResponseDTO gcResponse = new GatewayConnectorTransactionResponseDTO(
                new StatusModel(200, SUCCESS_STATUS_MSG),
                SUCCESS_STATUS_MSG,
                new TransactionResponseDTO(
                        "transaction-1",
                        100.00,
                        "1",
                        "2",
                        TransactionStatus.SUCCESS,
                        SUCCESS_STATUS_MSG,
                        LocalDateTime.now()
                )
        );

        // Mock gateway client to return successful response
        when(gatewayConnectorFeignClient.transferWalletMoney(any()))
                .thenReturn(gcResponse);
        when(transactionRepo.findById(any())).thenReturn(Optional.empty());
        when(streamBridge.send(
                anyString(),
                any(MessageInfoDto.class)
        )).thenReturn(true);

        // Perform the transfer
        WalletTransactionResponseDTO response = walletService.transferMoney(validTransferRequest, userId);

        // Verify interactions and assertions
        verify(transactionRepo, times(2)).save(any());
        verify(streamBridge).send(eq("send-communication-out-0"), any());

        assertNotNull(response);
        assertEquals(validTransferRequest.transactionId(), response.transactionId());
        assertEquals(validTransferRequest.amount(), response.amount());
        assertEquals(TransactionStatus.SUCCESS, response.status());
    }

    @Test
    void transferMoneyDuplicateTransactionThrowsInvalidFieldException() {
        // Simulate existing transaction
        when(transactionRepo.findById(validTransferRequest.transactionId()))
                .thenReturn(Optional.of(mock(Transaction.class)));

        // Verify exception is thrown
        assertThrows(InvalidFieldException.class, () ->
                walletService.transferMoney(validTransferRequest, userId)
        );
    }

    @Test
    void transferMoneySameWalletTransferThrowsInvalidFieldException() {
        // Create request with same source and destination wallet
        WalletTransferRequestDto sameWalletRequest = new WalletTransferRequestDto(
                "TXN002",
                1L,
                1L,
                500.00
        );

        // Verify exception is thrown
        assertThrows(InvalidFieldException.class, () ->
                walletService.transferMoney(sameWalletRequest, userId)
        );
    }

    @Test
    void transferMoneyInvalidAmountThrowsInvalidFieldException() {
        // Test with amount greater than max allowed
        WalletTransferRequestDto invalidAmountRequest = new WalletTransferRequestDto(
                "TXN003",
                1L,
                2L,
                1_00_001.00
        );

        // Verify exception is thrown
        assertThrows(InvalidFieldException.class, () ->
                walletService.transferMoney(invalidAmountRequest, userId)
        );
    }

    @Test
    void transferMoneyGatewayFailureHandlesFailureCorrectly() {
        // Simulate gateway connector throwing an ApiException
        when(gatewayConnectorFeignClient.transferWalletMoney(any()))
                .thenThrow(new ApiException(503, "Gateway error", "UNKNOWN_STATUS"));
        when(streamBridge.send(
                anyString(),
                any(MessageInfoDto.class)
        )).thenReturn(true);
        when(transactionRepo.save(any())).thenReturn(mock(Transaction.class));

        // Verify exception is rethrown and failure is handled
        assertThrows(ApiException.class, () ->
                walletService.transferMoney(validTransferRequest, userId)
        );

        verify(transactionRepo, times(2)).save(any());
        // Verify transaction is saved with FAILED status

        // Verify notification is sent
        verify(streamBridge).send(eq("send-communication-out-0"), any());
    }

}