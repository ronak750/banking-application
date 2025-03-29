package com.transactions.transactions.services;

import com.transactions.transactions.clinets.GatewayConnectorFeignClient;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.response.*;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.exception.ApiException;
import com.transactions.transactions.exception.InvalidFieldException;
import com.transactions.transactions.repos.TransactionRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UPIServiceTests {

    @Mock
    private GatewayConnectorFeignClient gatewayConnectorFeignClient;
    @Mock
    private StreamBridge streamBridge;
    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private UPIService upiService;

    private static final String VALID_UPI = "valid-upi-id";
    private static final String INVALID_UPI = "invalid-upi-id";

    @Test
    void testValidateUPIidValidUPIidReturnsTrue() {

        var gatewayConnectorValidationResponseDTO = new GatewayConnectorValidationResponseDTO(
                new StatusModel(200, "success"),
                "VALID ID",
                new ValidationResponseDTO(true)
        );
        // Arrange
        when(gatewayConnectorFeignClient.validateUPI(VALID_UPI)).thenReturn(gatewayConnectorValidationResponseDTO);

        // Act

        ValidationResponseDTO result = upiService.validateUPIid(VALID_UPI);

        // Assert
        assertTrue(result.getIsValid());
    }

    @Test
    void testValidateUPIidInvalidUPIidReturnsFalse() {
        // Arrange
        var gatewayConnectorValidationResponseDTO = new GatewayConnectorValidationResponseDTO(
                new StatusModel(200, "success"),
                "INVALID ID",
                new ValidationResponseDTO(false)
        );
        // Arrange
        when(gatewayConnectorFeignClient.validateUPI(INVALID_UPI)).thenReturn(gatewayConnectorValidationResponseDTO);

        // Act

        ValidationResponseDTO result = upiService.validateUPIid(INVALID_UPI);

        // Assert
        assertFalse(result.getIsValid());
    }

    @Test
    void testGetBalanceValidUPIidReturnsBalance() {
        // Arrange
        UPIBalanceCheckRequestDto balanceCheckRequestDto = new UPIBalanceCheckRequestDto(
                VALID_UPI,
                1234
        );

        var gatewayResponse= new GatewayConnectorBalanceResponseDTO(
            new StatusModel(200, "success"),
            "Balance Fetched Successfully",
            new BalanceResponseDTO(new BigDecimal("100.00"))
        );
        BigDecimal balance = BigDecimal.valueOf(100.0);
        when(gatewayConnectorFeignClient.checkUPIBalance(balanceCheckRequestDto)).thenReturn(gatewayResponse);

        // Act
        BigDecimal result = upiService.getBalance(balanceCheckRequestDto);

        // Assert
        assertEquals(balance.doubleValue(), result.doubleValue());
    }

    @Test
    void testGetBalanceInvalidUPIidThrowsTransactionRequestFailedException() {
        // Arrange

        UPIBalanceCheckRequestDto balanceCheckRequestDto = new UPIBalanceCheckRequestDto(
                INVALID_UPI,
                1234
        );
        when(gatewayConnectorFeignClient.checkUPIBalance(balanceCheckRequestDto)).thenThrow(ApiException.class);

        // Act and Assert
        assertThrows(ApiException.class, () -> upiService.getBalance(balanceCheckRequestDto));
    }

    @Test
    void testGetTransactionsValidUPIidReturnsTransactions() {
        // Arrange

        List<Transaction> transactions = List.of(
                Transaction.builder()
                    .transactionId("transaction-2")
                    .sourceId(VALID_UPI)
                    .destinationId("dummy-upi-id")
                     .transactionType(TransactionType.UPI)
                    .amount(100.00)
                    .build()
        );
        Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 20), transactions.size());
        var pageable = Pageable.ofSize(20).withPage(0);
        when(transactionRepo.findBySourceIdOrDestinationIdAndTransactionType(VALID_UPI, VALID_UPI, TransactionType.UPI, pageable)).thenReturn(transactionPage);

        // Act
        List<TransactionResponseDTO> result = upiService.getTransactions(VALID_UPI, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTransactionsInvalidUPIidReturnsEmptyList() {
        // Arrange

        Page<Transaction> transactionPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        var pageable = Pageable.ofSize(20).withPage(0);

        when(transactionRepo.findBySourceIdOrDestinationIdAndTransactionType(INVALID_UPI, INVALID_UPI,  TransactionType.UPI, pageable)).thenReturn(transactionPage);

        // Act
        List<TransactionResponseDTO> result = upiService.getTransactions(INVALID_UPI, Pageable.ofSize(20));

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testTransferValidTransferRequestReturnsTransactionResponseDTO() throws ApiException {
        // Arrange
        UPITransferRequestDTO transferRequest = new UPITransferRequestDTO(
                "transation-1",
                "from-upi-id",
                "to-upi-id",
                100.00,
                1234
                );
        String userId = "user-1";

        TransactionResponseDTO gcTransactionResponseDto = TransactionResponseDTO.builder()
                .status(TransactionStatus.SUCCESS)
                .remarks("Transaction successful")
                .build();
        var gcResponse = new GatewayConnectorTransactionResponseDTO(
                new StatusModel(200, "success"),
                "Transaction successful",
                gcTransactionResponseDto
        );
        when(gatewayConnectorFeignClient.transferUpiMoney(transferRequest)).thenReturn(gcResponse);

        // Act
        TransactionResponseDTO result = upiService.transfer(transferRequest, userId);

        // Assert
        assertNotNull(result);
        assertEquals(gcTransactionResponseDto.getStatus(), result.getStatus());
        assertEquals(gcTransactionResponseDto.getRemarks(), result.getRemarks());
    }

    @Test
    void testTransferInvalidAmountThrowsInvalidFieldException() {
        // Arrange
        UPITransferRequestDTO transferRequest = new UPITransferRequestDTO(
                "transation-1",
                "from-upi-id",
                "to-upi-id",
                -100.00,
                1234
        );
        String userId = "user-1";

        // Act and Assert
        assertThrows(InvalidFieldException.class, () -> upiService.transfer(transferRequest, userId));
    }

    @Test
    void testTransferSameToAndFromUpiIdThrowsInvalidFieldException() {
        // Arrange
        UPITransferRequestDTO transferRequest = new UPITransferRequestDTO(
                "transation-1",
                VALID_UPI,
                VALID_UPI,
                100.00,
                1234
        );
        String userId = "user-1";

        // Act and Assert
        assertThrows(InvalidFieldException.class, () -> upiService.transfer(transferRequest, userId));
    }

    @Test
    void testTransferRepeatTransactionIdThrowsInvalidFieldException() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .transactionId("transation-1")
                .build();

        UPITransferRequestDTO transferRequest = new UPITransferRequestDTO(
                "transation-1",
                VALID_UPI,
                VALID_UPI,
                100.00,
                1234
        );
        String userId = "user-1";

        when(transactionRepo.findById(transferRequest.transactionId())).thenReturn(Optional.of(transaction));

        // Act and Assert
        assertThrows(InvalidFieldException.class, () -> upiService.transfer(transferRequest, userId));
    }

    @Test
    void testTransferInvalidTransferRequestThrowsApiException() {
        // Arrange
        UPITransferRequestDTO transferRequest = new UPITransferRequestDTO(
                "transation-1",
                "from-upi-id",
                "to-upi-id",
                100.00,
                1234
        );
        String userId = "user-1";
        when(gatewayConnectorFeignClient.transferUpiMoney(transferRequest)).thenThrow(ApiException.class);

        // Act and Assert
        assertThrows(ApiException.class, () -> upiService.transfer(transferRequest, userId));
    }

}