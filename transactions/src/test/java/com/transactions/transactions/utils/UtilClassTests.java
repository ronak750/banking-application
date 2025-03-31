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

import static com.transactions.transactions.constant.Constants.SOMETHING_WENT_WRONG_MSG;
import static org.junit.jupiter.api.Assertions.*;

class UtilClassTests {

    public static final String ERROR_MESSAGE = "Error message";
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String SUCCESS = "Success";
    public static final String SUCCESS_MESSAGE = "Success message";
    public static final String TRANSACTION_ID = "123";
    public static final String TEST_TRANSACTIONMSG = "Test transaction";

    @Test
    void testConvertTransactionToWalletTransactionResponseDto() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(TRANSACTION_ID);
        transaction.setAmount(10);
        transaction.setSourceId(SOURCE);
        transaction.setDestinationId(DESTINATION);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(TEST_TRANSACTIONMSG);
        transaction.setCreatedAt(null);

        WalletTransactionResponseDTO response = UtilClass.convertTransactionToWalletTransactionResponseDto(transaction);

        assertNotNull(response);
        assertEquals(TRANSACTION_ID, response.transactionId());
        assertEquals(BigDecimal.TEN.doubleValue(), response.amount());
        assertEquals(SOURCE, response.fromWalletId());
        assertEquals(DESTINATION, response.toWalletId());
        assertEquals(TransactionStatus.SUCCESS, response.status());
        assertEquals(TEST_TRANSACTIONMSG, response.remarks());
        assertNotNull(response.time());
    }

    @Test
    void testFetchBalanceFromGatewayConnectorBalanceResponseDTOValidResponse() {
        GatewayConnectorBalanceResponseDTO responseDTO = new GatewayConnectorBalanceResponseDTO(
                new StatusModel(200, SUCCESS),
                SUCCESS_MESSAGE,
                new BalanceResponseDTO(BigDecimal.TEN)
        );

        BigDecimal balance = UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(responseDTO);

        assertEquals(BigDecimal.TEN.doubleValue(), balance.doubleValue());
    }

    @Test
    void testFetchBalanceFromGatewayConnectorBalanceResponseDTONullResponseWithStatusModel() {
        GatewayConnectorBalanceResponseDTO responseDTO = new GatewayConnectorBalanceResponseDTO(
                new StatusModel(400, "Error"),
                ERROR_MESSAGE,
                null
        );

        ApiException exception = assertThrows(ApiException.class, () ->
                UtilClass.fetchBalanceFromGatewayConnectorBalanceResponseDTO(responseDTO));

        assertEquals(400, exception.getStatusCode());
        assertEquals(ERROR_MESSAGE, exception.getErrorMessage());
    }

    @Test
    void testGetPageableValidInputs() {
        Pageable pageable = UtilClass.getPageable("2", "20");

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void testGetPageableInvalidPageNumberStrings() {
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () ->
                UtilClass.getPageable("ABC", "20"));

        assertEquals("Numeric value expected for pagination details", exception.getMessage());
    }

    @Test
    void testGetPageableInvalidPageNumber() {
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
    void testGetPageableInvalidPageSizeOverLimit() {
        InvalidFieldException exception = assertThrows(InvalidFieldException.class, () ->
                UtilClass.getPageable("1", "10000"));

        assertEquals("Page size must be greater than 0 and less than 500 in query", exception.getMessage());
    }

    @Test
    void testFetchTransactionFromGatewayConnectorResponseDTOValidResponse() {
        GatewayConnectorTransactionResponseDTO responseDTO = new GatewayConnectorTransactionResponseDTO(
                new StatusModel(200, SUCCESS),
                SUCCESS_MESSAGE,
                new TransactionResponseDTO(TRANSACTION_ID, 10.00, SOURCE, DESTINATION, TransactionStatus.SUCCESS, TEST_TRANSACTIONMSG, LocalDateTime.now())
        );

        TransactionResponseDTO transaction = UtilClass.fetchTransactionFromGatewayConnectorResponseDTO(responseDTO);

        assertNotNull(transaction);
        assertEquals(TRANSACTION_ID, transaction.getTransactionId());
        assertEquals(BigDecimal.TEN.doubleValue(), transaction.getAmount());
    }

    @Test
    void testFetchTransactionFromGatewayConnectorResponseDTOMissingResponseMsg() {
        GatewayConnectorTransactionResponseDTO responseDTO = new GatewayConnectorTransactionResponseDTO(
                new StatusModel(200, SUCCESS),
                null,
                new TransactionResponseDTO(TRANSACTION_ID, 10.00, SOURCE, DESTINATION, TransactionStatus.SUCCESS, TEST_TRANSACTIONMSG, LocalDateTime.now())
        );

        TransactionResponseDTO transaction = UtilClass.fetchTransactionFromGatewayConnectorResponseDTO(responseDTO);

        assertNotNull(transaction);
        assertEquals(TRANSACTION_ID, transaction.getTransactionId());
        assertEquals(BigDecimal.TEN.doubleValue(), transaction.getAmount());
    }

    @Test
    void testFetchTransactionFromGatewayConnectorResponseDTOMissingResponseBody() {
        GatewayConnectorTransactionResponseDTO responseDTO = new GatewayConnectorTransactionResponseDTO(
                null,
                SUCCESS_MESSAGE,
                null
        );

        ApiException exception = assertThrows(ApiException.class, () ->
                UtilClass.fetchTransactionFromGatewayConnectorResponseDTO(responseDTO));

        assertEquals(503, exception.getStatusCode());
        assertEquals(SOMETHING_WENT_WRONG_MSG, exception.getErrorMessage());
    }

    @Test
    void testGetListOfTransactionsFromGatewayConnectorResponseValidResponse() {
        List<TransactionResponseDTO> transactions = List.of(
                new TransactionResponseDTO(TRANSACTION_ID, 10.00, SOURCE, DESTINATION, TransactionStatus.SUCCESS, TEST_TRANSACTIONMSG, LocalDateTime.now())
        );

        GatewayConnectorTransactionListResponseDTO responseDTO = new GatewayConnectorTransactionListResponseDTO(
                new StatusModel(200, SUCCESS),
                SUCCESS_MESSAGE,
                transactions
        );

        List<TransactionResponseDTO> result = UtilClass.getListOfTransactionsFromGatewayConnectorResponse(responseDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TRANSACTION_ID, result.get(0).getTransactionId());
    }

    @Test
    void testGetListOfTransactionsFromGatewayConnectorResponseValidResponseMissingResponseMsg() {
        List<TransactionResponseDTO> transactions = List.of(
                new TransactionResponseDTO(TRANSACTION_ID, 10.00, SOURCE, DESTINATION, TransactionStatus.SUCCESS, TEST_TRANSACTIONMSG, LocalDateTime.now())
        );

        GatewayConnectorTransactionListResponseDTO responseDTO = new GatewayConnectorTransactionListResponseDTO(
                new StatusModel(200, SUCCESS),
                null,
                transactions
        );

        List<TransactionResponseDTO> result = UtilClass.getListOfTransactionsFromGatewayConnectorResponse(responseDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TRANSACTION_ID, result.get(0).getTransactionId());
    }

    @Test
    void testGetListOfTransactionsFromGatewayConnectorResponseValidResponseMissingResponseBody() {

        GatewayConnectorTransactionListResponseDTO responseDTO = new GatewayConnectorTransactionListResponseDTO(
                new StatusModel(200, SUCCESS),
                null,
                null
        );
        ApiException exception = assertThrows(ApiException.class, () ->
                UtilClass.getListOfTransactionsFromGatewayConnectorResponse(responseDTO));

        assertEquals(400, exception.getStatusCode());
        assertEquals(ERROR_MESSAGE, exception.getErrorMessage());
    }
}
