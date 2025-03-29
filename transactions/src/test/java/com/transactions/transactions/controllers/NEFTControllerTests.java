package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.services.NEFTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NEFTControllerTests {


    @Mock
    private NEFTService neftService;

    @InjectMocks
    private NEFTController neftController;

    private NEFTtransferRequestDto validTransferRequest;
    private String userId;

    public static final String TRANSACTION_ID = "TXN123";
    public static final String FROM_ACCOUNT_NUMBER = "1234567890";
    public static final String TO_ACCOUNT_NUMBER = "0987654321";
    public static final String SUCCESS = "SUCCESS";

    @BeforeEach
    void setUp() {
        // Setup a valid transfer request
        validTransferRequest = new NEFTtransferRequestDto(
                TRANSACTION_ID,
                FROM_ACCOUNT_NUMBER,
                TO_ACCOUNT_NUMBER,
                "BANK001",
                "BANK002",
                5000.00
                );

        userId = "USER123";
    }

    @Test
    void testTransferMoney_Successful() {
        // Prepare mock response
        TransactionResponseDTO mockResponse = TransactionResponseDTO.builder()
                .transactionId(TRANSACTION_ID)
                .amount(5000.00)
                .from(FROM_ACCOUNT_NUMBER)
                .to(TO_ACCOUNT_NUMBER)
                .status(TransactionStatus.PENDING)
                .time(LocalDateTime.now())
                .build();

        // Mock service method
        when(neftService.transferMoney(validTransferRequest, userId)).thenReturn(mockResponse);

        // Invoke controller method
        ResponseEntity<APIResponseDTO> responseEntity = neftController.transferMoney(userId, validTransferRequest);

        // Verify
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Request submitted successfully", apiResponse.getResponseMsg());

        StatusModel statusModel = apiResponse.getStatusModel();
        assertNotNull(statusModel);
        assertEquals(202, statusModel.getStatusCode());
        assertEquals("PENDING", statusModel.getStatusMsg());
    }

    @Test
    void testGetTransactionDetails_Success() {
        // Prepare mock response
        TransactionResponseDTO mockResponse = TransactionResponseDTO.builder()
                .transactionId(TRANSACTION_ID)
                .amount(5000.00)
                .from(FROM_ACCOUNT_NUMBER)
                .to(TO_ACCOUNT_NUMBER)
                .status(TransactionStatus.SUCCESS)
                .time(LocalDateTime.now())
                .build();

        // Mock service method
        when(neftService.getTransactionDetailsByTransactionId(TRANSACTION_ID))
                .thenReturn(mockResponse);

        // Invoke controller method
        ResponseEntity<APIResponseDTO> responseEntity =
                neftController.getTransactionDetailsByTransactionId(TRANSACTION_ID);

        // Verify
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Transaction completed successfully", apiResponse.getResponseMsg());

        StatusModel statusModel = apiResponse.getStatusModel();
        assertNotNull(statusModel);
        assertEquals(200, statusModel.getStatusCode());
        assertEquals(SUCCESS, statusModel.getStatusMsg());
    }

    @Test
    void testGetTransactionDetails_Failed() {
        // Prepare mock response for failed transaction
        TransactionResponseDTO mockResponse = TransactionResponseDTO.builder()
                .transactionId(TRANSACTION_ID)
                .amount(5000.00)
                .from(FROM_ACCOUNT_NUMBER)
                .to(TO_ACCOUNT_NUMBER)
                .status(TransactionStatus.FAILED)
                .time(LocalDateTime.now())
                .build();

        // Mock service method
        when(neftService.getTransactionDetailsByTransactionId(TRANSACTION_ID))
                .thenReturn(mockResponse);

        // Invoke controller method
        ResponseEntity<APIResponseDTO> responseEntity =
                neftController.getTransactionDetailsByTransactionId(TRANSACTION_ID);

        // Verify
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Transaction failed", apiResponse.getResponseMsg());

        StatusModel statusModel = apiResponse.getStatusModel();
        assertNotNull(statusModel);
        assertEquals(400, statusModel.getStatusCode());
        assertEquals("FAILED", statusModel.getStatusMsg());
    }

    @Test
    void testGetTransactionDetails_Pending() {
        // Prepare mock response for pending transaction
        TransactionResponseDTO mockResponse = TransactionResponseDTO.builder()
                .transactionId(TRANSACTION_ID)
                .amount(5000.00)
                .from(FROM_ACCOUNT_NUMBER)
                .to(TO_ACCOUNT_NUMBER)
                .status(TransactionStatus.PENDING)
                .time(LocalDateTime.now())
                .build();

        // Mock service method
        when(neftService.getTransactionDetailsByTransactionId(TRANSACTION_ID))
                .thenReturn(mockResponse);

        // Invoke controller method
        ResponseEntity<APIResponseDTO> responseEntity =
                neftController.getTransactionDetailsByTransactionId(TRANSACTION_ID);

        // Verify
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals("Transaction Pending", apiResponse.getResponseMsg());

        StatusModel statusModel = apiResponse.getStatusModel();
        assertNotNull(statusModel);
        assertEquals(200, statusModel.getStatusCode());
        assertEquals(SUCCESS, statusModel.getStatusMsg());
    }
}