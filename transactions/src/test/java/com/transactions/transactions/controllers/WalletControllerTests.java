package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.request.WalletTransactionResponseDTO;
import com.transactions.transactions.dto.request.WalletTransferRequestDto;
import com.transactions.transactions.dto.response.BalanceResponseDTO;
import com.transactions.transactions.services.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class WalletControllerTests {

    private static final Long TEST_WALLET_ID = 1L;
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_TRANSACTION_ID = "tx123";
    private static final double TEST_AMOUNT = 100.00;
    private static final String TEST_REMARKS = "Test Transfer";

    @Mock
    WalletService walletService;

    @InjectMocks
    WalletController walletController;

    @Test
    void testGetBalance() {
        // Arrange
        BigDecimal expectedBalance = BigDecimal.valueOf(500.00).setScale(2);
        when(walletService.getBalance(TEST_WALLET_ID)).thenReturn(expectedBalance);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = walletController.getBalance(TEST_WALLET_ID);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Wallet balance successfully fetched", apiResponse.getResponseMsg());

        BalanceResponseDTO balanceResponse = (BalanceResponseDTO) apiResponse.getResponse();
        assertEquals(expectedBalance, balanceResponse.getBalance());
    }

    @Test
    void testTransferMoneySuccess() {
        // Arrange
        WalletTransferRequestDto transferRequest = new WalletTransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_WALLET_ID,
                TEST_WALLET_ID + 1,
                TEST_AMOUNT
        );

        WalletTransactionResponseDTO successResponse = new WalletTransactionResponseDTO(
                TEST_TRANSACTION_ID,
                TEST_AMOUNT,
                TEST_WALLET_ID.toString(),
                String.valueOf(TEST_WALLET_ID + 1),
                TransactionStatus.SUCCESS,
                TEST_REMARKS,
                LocalDateTime.now()
        );

        when(walletService.transferMoney(transferRequest, TEST_USER_ID)).thenReturn(successResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = walletController.transferMoney(TEST_USER_ID, transferRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Money transfer request processed", apiResponse.getResponseMsg());

        WalletTransactionResponseDTO transactionResponse = (WalletTransactionResponseDTO) apiResponse.getResponse();
        assertEquals(TransactionStatus.SUCCESS, transactionResponse.status());
    }

    @Test
    void testTransferMoneyFailure() {
        // Arrange
        WalletTransferRequestDto transferRequest = new WalletTransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_WALLET_ID,
                TEST_WALLET_ID + 1,
                TEST_AMOUNT
        );

        WalletTransactionResponseDTO failureResponse = new WalletTransactionResponseDTO(
                TEST_TRANSACTION_ID,
                TEST_AMOUNT,
                TEST_WALLET_ID.toString(),
                String.valueOf(TEST_WALLET_ID + 1),
                TransactionStatus.FAILED,
                TEST_REMARKS,
                LocalDateTime.now()
        );

        when(walletService.transferMoney(transferRequest, TEST_USER_ID)).thenReturn(failureResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = walletController.transferMoney(TEST_USER_ID, transferRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(400, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Money transfer request failed", apiResponse.getResponseMsg());

        WalletTransactionResponseDTO transactionResponse = (WalletTransactionResponseDTO) apiResponse.getResponse();
        assertEquals(TransactionStatus.FAILED, transactionResponse.status());
    }

    @Test
    void testGetTransactionsWithContent() {
        // Arrange
        String walletId = TEST_WALLET_ID.toString();

        List<WalletTransactionResponseDTO> transactions = new ArrayList<>();
        WalletTransactionResponseDTO transaction = new WalletTransactionResponseDTO(
                TEST_TRANSACTION_ID,
                TEST_AMOUNT,
                TEST_WALLET_ID.toString(),
                String.valueOf(TEST_WALLET_ID + 1),
                TransactionStatus.SUCCESS,
                TEST_REMARKS,
                LocalDateTime.now()
        );
        transactions.add(transaction);

        when(walletService.getTransactions(anyString(), any(Pageable.class))).thenReturn(transactions);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = walletController.getTransactions(
                walletId,
                "10",
                "1"
        );

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Wallet transactions fetched successfully", apiResponse.getResponseMsg());

        List<WalletTransactionResponseDTO> responseTransactions =
                (List<WalletTransactionResponseDTO>) apiResponse.getResponse();
        assertFalse(responseTransactions.isEmpty());
    }

    @Test
    void testGetTransactionsEmpty() {
        // Arrange
        String walletId = TEST_WALLET_ID.toString();

        List<WalletTransactionResponseDTO> transactions = new ArrayList<>();

        when(walletService.getTransactions(anyString(), any(Pageable.class))).thenReturn(transactions);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = walletController.getTransactions(
                walletId,
                "10",
                "1"
        );

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("No Wallet transactions available for provided filters", apiResponse.getResponseMsg());

        List<WalletTransactionResponseDTO> responseTransactions =
                (List<WalletTransactionResponseDTO>) apiResponse.getResponse();
        assertTrue(responseTransactions.isEmpty());
    }

}