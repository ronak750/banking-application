package com.transactions.transactions.utils;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.response.*;
import com.transactions.transactions.dto.request.WalletTransactionResponseDTO;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.exception.ApiException;
import com.transactions.transactions.exception.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilClassTest {

    @Test
    void testConvertTransactionToWalletTransactionResponseDto() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("123");
        transaction.setAmount(10);
        transaction.setSourceId("source");
        transaction.setDestinationId("destination");
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(null);

        WalletTransactionResponseDTO response = UtilClass.convertTransactionToWalletTransactionResponseDto(transaction);

        assertNotNull(response);
        assertEquals("123", response.transactionId());
        assertEquals(BigDecimal.TEN.doubleValue(), response.amount());
        assertEquals("source", response.fromWalletId());
        assertEquals("destination", response.toWalletId());
        assertEquals(TransactionStatus.SUCCESS, response.status());
        assertEquals("Test transaction", response.remarks());
    }

    @Test
    void testFetchBalanceFromGatewayConnectorBalanceResponseDTOValidResponse() {
        GatewayConnectorBalanceResponseDTO responseDTO = new GatewayConnectorBalanceResponseDTO(
            new StatusModel(200, "Success"),
            "Success message",
            new BalanceResponseDTO(BigDecimal.TEN)
        );

        BigDecimal balance = UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(responseDTO);

        assertEquals(BigDecimal.TEN.doubleValue(), balance.doubleValue());
    }

    @Test
    void testFetchBalanceFromGatewayConnectorBalanceResponseDTONullResponseWithStatusModel() {
        GatewayConnectorBalanceResponseDTO responseDTO = new GatewayConnectorBalanceResponseDTO(
            new StatusModel(400, "Error"),
            "Error message",
            null
        );

        ApiException exception = assertThrows(ApiException.class, () ->
                UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(responseDTO));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Error message", exception.getErrorMessage());
    }

    @Test
    void testGetPageable_ValidInputs() {
        Pageable pageable = UtilClass.getPageable("2", "20");

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void testGetPageable_InvalidPageNumber() {
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () ->
                UtilClass.getPageable("-1", "20"));

        assertEquals("Page number must be positive number", exception.getMessage());
    }

    @Test
    void testGetPageableInvalidPageSize() {
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () ->
                UtilClass.getPageable("1", "0"));

        assertEquals("Page size must be greater than 0 and less than 500 in query", exception.getMessage());
    }

    @Test
    void testFetchTransactionFromGatewayConnectorResponseDTOValidResponse() {
        GatewayConnectorTransactionResponseDTO responseDTO = new GatewayConnectorTransactionResponseDTO(
            new StatusModel(200, "Success"),
            "Success message",
            new TransactionResponseDTO("123", 10.00, "source", "destination", TransactionStatus.SUCCESS, "Test transaction", LocalDateTime.now())
        );

        TransactionResponseDTO transaction = UtilClass.fetchTransactionFromGatewayConnectorResponseDTO(responseDTO);

        assertNotNull(transaction);
        assertEquals("123", transaction.getTransactionId());
        assertEquals(BigDecimal.TEN.doubleValue(), transaction.getAmount());
    }

    @Test
    void testGetListOfTransactionsFromGatewayConnectorResponseValidResponse() {
        List<TransactionResponseDTO> transactions = List.of(
            new TransactionResponseDTO("123", 10.00, "source", "destination", TransactionStatus.SUCCESS, "Test transaction", LocalDateTime.now())
        );

        GatewayConnectorTransactionListResponseDTO responseDTO = new GatewayConnectorTransactionListResponseDTO(
            new StatusModel(200, "Success"),
            "Success message",
            transactions
        );

        List<TransactionResponseDTO> result = UtilClass.getListOfTransactionsFromGatewayConnectorResponse(responseDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getTransactionId());
    }
}
