package com.transactions.transactions.services;

import com.transactions.transactions.constant.Constants;
import com.transactions.transactions.dto.MessageInfoDto;
import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.entity.NEFTProcessingTransaction;
import com.transactions.transactions.entity.Transaction;
import com.transactions.transactions.entity.TransactionType;
import com.transactions.transactions.exception.InvalidFieldException;
import com.transactions.transactions.repos.NEFTTransactionProcessingQueueRepo;
import com.transactions.transactions.repos.TransactionRepo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Optional;

import static com.transactions.transactions.constant.Constants.INVALID_AMOUNT_TRANSFER_ERROR_MSG;
import static com.transactions.transactions.constant.Constants.SAME_ACCOUNT_TRANSFER_ERROR_MSG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NEFTServiceTests {


    @Mock
    private TransactionRepo transactionRepo;
    @Mock
    private NEFTTransactionProcessingQueueRepo neftTransactionProcessingQueueRepo;
    @Mock
    private StreamBridge streamBridge;


    @InjectMocks
    private NEFTService transactionService;

    public static final String ACCT_001 = "ACCT001";
    public static final String ACCT_002 = "ACCT002";
    // Constants for test data
    private static final String TEST_USER_ID = "USER123";
    private static final String TEST_TRANSACTION_ID = "TXNEFT001";
    private static final double VALID_AMOUNT = 50000.00;
    private static final double INVALID_AMOUNT_ZERO = 0.00;
    private static final double INVALID_AMOUNT_OVER_LIMIT = 1_00_001.00;
    private static final String TEST_FROM_ACCOUNT = "1234567890";
    private static final String TEST_FROM_IFSC = "BANK001";
    private static final String TEST_TO_ACCOUNT = "0987654321";
    private static final String TEST_TO_IFSC = "BANK002";

    @Test
    void testGetTransactionDetailsByTransactionIdValidTransaction() {
        // Arrange
        String transactionId = "TX123";
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .transactionType(TransactionType.NEFT)
                .sourceId(ACCT_001)
                .destinationId(ACCT_002)
                .amount(500.00)
                .transactionStatus(TransactionStatus.SUCCESS)
                .description("Transfer Success")
                .build();

        when(transactionRepo.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        TransactionResponseDTO responseDTO = transactionService.getTransactionDetailsByTransactionId(transactionId);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(transactionId, responseDTO.getTransactionId());
        assertEquals(500.00, responseDTO.getAmount());
        assertEquals(ACCT_001, responseDTO.getFrom());
        assertEquals(ACCT_002, responseDTO.getTo());
        assertEquals(TransactionStatus.SUCCESS, responseDTO.getStatus());

        // Verify repository method was called
        verify(transactionRepo, times(1)).findById(transactionId);
    }

    @Test
    void testGetTransactionDetailsByTransactionIdNonExistentTransaction() {
        // Arrange
        String transactionId = "NONEXISTENT";

        when(transactionRepo.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidFieldException exception = assertThrows(InvalidFieldException.class,
                () -> transactionService.getTransactionDetailsByTransactionId(transactionId));

        assertEquals("Transaction with id NONEXISTENT does not exist", exception.getMessage());

        // Verify repository method was called
        verify(transactionRepo, times(1)).findById(transactionId);
    }


    @Test
    void testTransferMoneySuccessfulTransaction() {
        // Arrange
        NEFTtransferRequestDto requestDto = new NEFTtransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_FROM_ACCOUNT,
                TEST_TO_ACCOUNT,
                TEST_FROM_IFSC,
                TEST_TO_IFSC,
                VALID_AMOUNT
        );

        // Mock repository to return empty for duplicate check
        when(neftTransactionProcessingQueueRepo.findById(TEST_TRANSACTION_ID))
                .thenReturn(Optional.empty());
        when(streamBridge.send(
                anyString(),
                any(MessageInfoDto.class)
        )).thenReturn(true);

        // Act
        TransactionResponseDTO responseDTO = transactionService.transferMoney(requestDto, TEST_USER_ID);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(TEST_TRANSACTION_ID, responseDTO.getTransactionId());
        assertEquals(VALID_AMOUNT, responseDTO.getAmount());
        assertEquals(TEST_FROM_ACCOUNT + "-" + TEST_FROM_IFSC, responseDTO.getFrom());
        assertEquals(TEST_TO_ACCOUNT + "-" + TEST_TO_IFSC, responseDTO.getTo());
        assertEquals(TransactionStatus.PENDING, responseDTO.getStatus());
        assertNotNull(responseDTO.getTime());

        // Verify interactions
        verify(neftTransactionProcessingQueueRepo).findById(TEST_TRANSACTION_ID);
        verify(transactionRepo).save(any(Transaction.class));
        verify(neftTransactionProcessingQueueRepo).save(any(NEFTProcessingTransaction.class));
        verify(streamBridge, times(1)).send(eq("send-communication-out-0"), any(MessageInfoDto.class));

    }

    @Test
    void testTransferMoneyWithDuplicateTransaction() {
        // Arrange
        NEFTtransferRequestDto requestDto = new NEFTtransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_FROM_ACCOUNT,
                TEST_TO_ACCOUNT,
                TEST_FROM_IFSC,
                TEST_TO_IFSC,
                VALID_AMOUNT
        );

        // Mock repository to return existing transaction
        when(neftTransactionProcessingQueueRepo.findById(TEST_TRANSACTION_ID))
                .thenReturn(Optional.of(mock(NEFTProcessingTransaction.class)));

        // Act & Assert
        InvalidFieldException exception = assertThrows(
                InvalidFieldException.class,
                () -> transactionService.transferMoney(requestDto, TEST_USER_ID)
        );
        assertEquals(Constants.DUPLICATE_TRANSACTION_ERROR_MSG, exception.getMessage());

        // Verify no further interactions
        verify(neftTransactionProcessingQueueRepo).findById(TEST_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepo);
        verifyNoMoreInteractions(neftTransactionProcessingQueueRepo);
        verifyNoMoreInteractions(streamBridge);
    }

    @Test
    void testTransferMoneyWithInvalidAmountZero() {
        // Arrange
        NEFTtransferRequestDto requestDto = new NEFTtransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_FROM_ACCOUNT,
                TEST_TO_ACCOUNT,
                TEST_FROM_IFSC,
                TEST_TO_IFSC,
                INVALID_AMOUNT_ZERO
        );

        // Mock repository to return empty for duplicate check
        when(neftTransactionProcessingQueueRepo.findById(TEST_TRANSACTION_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        InvalidFieldException exception = assertThrows(
                InvalidFieldException.class,
                () -> transactionService.transferMoney(requestDto, TEST_USER_ID)
        );
        assertEquals(INVALID_AMOUNT_TRANSFER_ERROR_MSG, exception.getMessage());

        // Verify no further interactions
        verify(neftTransactionProcessingQueueRepo).findById(TEST_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepo);
        verifyNoMoreInteractions(neftTransactionProcessingQueueRepo);
        verifyNoMoreInteractions(streamBridge);
    }

    @Test
    void testTransferMoneyWithInvalidAmountOverLimit() {
        // Arrange
        NEFTtransferRequestDto requestDto = new NEFTtransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_FROM_ACCOUNT,
                TEST_TO_ACCOUNT,
                TEST_FROM_IFSC,
                TEST_TO_IFSC,
                INVALID_AMOUNT_OVER_LIMIT
        );

        // Mock repository to return empty for duplicate check
        when(neftTransactionProcessingQueueRepo.findById(TEST_TRANSACTION_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        InvalidFieldException exception = assertThrows(
                InvalidFieldException.class,
                () -> transactionService.transferMoney(requestDto, TEST_USER_ID)
        );
        assertEquals(INVALID_AMOUNT_TRANSFER_ERROR_MSG, exception.getMessage());

        // Verify no further interactions
        verify(neftTransactionProcessingQueueRepo).findById(TEST_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepo);
        verifyNoMoreInteractions(neftTransactionProcessingQueueRepo);
        verifyNoMoreInteractions(streamBridge);
    }

    @Test
    void testTransferMoneyWithSameAccountTransfer() {
        // Arrange
        NEFTtransferRequestDto requestDto = new NEFTtransferRequestDto(
                TEST_TRANSACTION_ID,
                TEST_FROM_ACCOUNT,
                TEST_FROM_ACCOUNT,
                TEST_FROM_IFSC,
                TEST_FROM_IFSC,
                VALID_AMOUNT
        );

        // Mock repository to return empty for duplicate check
        when(neftTransactionProcessingQueueRepo.findById(TEST_TRANSACTION_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        InvalidFieldException exception = assertThrows(
                InvalidFieldException.class,
                () -> transactionService.transferMoney(requestDto, TEST_USER_ID)
        );
        assertEquals(SAME_ACCOUNT_TRANSFER_ERROR_MSG, exception.getMessage());

        // Verify no further interactions
        verify(neftTransactionProcessingQueueRepo).findById(TEST_TRANSACTION_ID);
        verifyNoMoreInteractions(transactionRepo);
        verifyNoMoreInteractions(neftTransactionProcessingQueueRepo);
        verifyNoMoreInteractions(streamBridge);
    }

}