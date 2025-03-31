package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.UPIValidationRequestDTO;
import com.transactions.transactions.dto.response.BalanceResponseDTO;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.dto.response.ValidationResponseDTO;
import com.transactions.transactions.services.UPIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

class UPIControllerTest {

    @Mock
    private UPIService upiService;

    @InjectMocks
    private UPIController upiController;

    @BeforeEach
    void setUp() {
        upiService = mock(UPIService.class);
        upiController = new UPIController(upiService);
    }

    @Test
    void testValidateUPIId() {
        UPIValidationRequestDTO requestDTO = new UPIValidationRequestDTO("test@upi");
        ValidationResponseDTO validationResponse = new ValidationResponseDTO(true);
        when(upiService.validateUPIid(requestDTO.upiId())).thenReturn(validationResponse);

        ResponseEntity<APIResponseDTO> response = upiController.validate(requestDTO);

        assertEquals(OK, response.getStatusCode());
        assertEquals("UPI ID is valid", response.getBody().getResponseMsg());
        assertEquals(validationResponse, response.getBody().getResponse());
        verify(upiService, times(1)).validateUPIid(requestDTO.upiId());
    }

    @Test
    void testGetBalance() {
        UPIBalanceCheckRequestDto requestDto = new UPIBalanceCheckRequestDto(
                "test@upi", // UPI ID
                1234        // PIN
        ); // Use the correct constructor
        BigDecimal balance = BigDecimal.valueOf(1000.50);
        when(upiService.getBalance(requestDto)).thenReturn(balance);

        ResponseEntity<APIResponseDTO> response = upiController.getBalance(requestDto);

        assertEquals(OK, response.getStatusCode());
        assertEquals("UPI balance successfully fetched", response.getBody().getResponseMsg());
        assertEquals(balance.setScale(2), ((BalanceResponseDTO) response.getBody().getResponse()).getBalance());
        verify(upiService, times(1)).getBalance(requestDto);
    }

    @Test
    void testTransferSuccess() {
        UPITransferRequestDTO requestDTO = new UPITransferRequestDTO(
                "txn123", // Transaction ID
                "test@upi", // From UPI ID
                "receiver@upi", // To UPI ID
                500.00, // Amount
                1234 // PIN
        );
        String userId = "12345";
        TransactionResponseDTO responseDTO = TransactionResponseDTO.builder()
                .transactionId("txn123")
                .amount(500.00)
                .from("test@upi")
                .to("receiver@upi")
                .status(TransactionStatus.SUCCESS)
                .remarks("Transaction successful")
                .time(LocalDateTime.now())
                .build();
        when(upiService.transfer(requestDTO, userId)).thenReturn(responseDTO);

        ResponseEntity<APIResponseDTO> response = upiController.transfer(userId, requestDTO);

        assertEquals(OK, response.getStatusCode());
        assertEquals("Money transfer request processed", response.getBody().getResponseMsg());
        assertEquals(responseDTO, response.getBody().getResponse());
        verify(upiService, times(1)).transfer(requestDTO, userId);
    }

    @Test
    void testTransferFailure() {
        UPITransferRequestDTO requestDTO = new UPITransferRequestDTO(
                "txn123", // Transaction ID
                "test@upi", // From UPI ID
                "receiver@upi", // To UPI ID
                500.00, // Amount
                1234 // PIN
        );
        String userId = "12345";
        TransactionResponseDTO responseDTO = TransactionResponseDTO.builder()
                .transactionId("txn123")
                .amount(500.00)
                .from("test@upi")
                .to("receiver@upi")
                .status(TransactionStatus.FAILED)
                .remarks("Transaction failed")
                .time(LocalDateTime.now())
                .build();
        when(upiService.transfer(requestDTO, userId)).thenReturn(responseDTO);

        ResponseEntity<APIResponseDTO> response = upiController.transfer(userId, requestDTO);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Money transfer request failed", response.getBody().getResponseMsg());
        assertEquals(responseDTO, response.getBody().getResponse());
        verify(upiService, times(1)).transfer(requestDTO, userId);
    }

    @Test
    void testGetTransactionsSuccess() {
        UPIValidationRequestDTO requestDTO = new UPIValidationRequestDTO("test@upi");
        List<TransactionResponseDTO> transactions = List.of(
                new TransactionResponseDTO(
                    "Transaction 1",
                    100.00,
                    "test@upi",
                    "receiver@upi",
                    TransactionStatus.SUCCESS,
                    "Transaction successful",
                    LocalDateTime.now()
                ),
                new TransactionResponseDTO(
                    "Transaction 2",
                    100.00,
                    "test@upi",
                    "receiver@upi",
                    TransactionStatus.FAILED,
                    "Transaction successful",
                    LocalDateTime.now()
                )
        );
        when(upiService.getTransactions(eq(requestDTO.upiId()), any())).thenReturn(transactions);

        ResponseEntity<APIResponseDTO> response = upiController.getTransactions(requestDTO, "10", "1");

        assertEquals(OK, response.getStatusCode());
        assertEquals("Transactions successfully fetched", response.getBody().getResponseMsg());
        assertEquals(transactions, response.getBody().getResponse());
        verify(upiService, times(1)).getTransactions(eq(requestDTO.upiId()), any());
    }

    @Test
    void testGetTransactionsNotFound() {
        UPIValidationRequestDTO requestDTO = new UPIValidationRequestDTO("test@upi");
        when(upiService.getTransactions(eq(requestDTO.upiId()), any())).thenReturn(Collections.emptyList());

        ResponseEntity<APIResponseDTO> response = upiController.getTransactions(requestDTO, "10", "1");

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertEquals("No transactions found", response.getBody().getResponseMsg());
        verify(upiService, times(1)).getTransactions(eq(requestDTO.upiId()), any());
    }
}
