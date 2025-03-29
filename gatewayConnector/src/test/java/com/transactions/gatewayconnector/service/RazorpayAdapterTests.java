package com.transactions.gatewayconnector.service;


import com.transactions.gatewayconnector.dto.RazorpayTransactionResponseDTO;
import com.transactions.gatewayconnector.dto.TransactionStatus;
import com.transactions.gatewayconnector.dto.request.NeftTransferRequestDto;
import com.transactions.gatewayconnector.dto.request.UPITransferRequestDTO;
import com.transactions.gatewayconnector.dto.request.WalletTransferRequestDto;
import com.transactions.gatewayconnector.dto.response.BalanceResponseDTO;
import com.transactions.gatewayconnector.dto.response.TransactionResponseDto;
import com.transactions.gatewayconnector.dto.response.ValidationResponseDTO;
import com.transactions.gatewayconnector.paymentgateway.Razorpay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayAdapterTests {

    @Mock
    private Razorpay razorpay;

    @InjectMocks
    private RazorpayAdapter razorpayAdapter;

    private WalletTransferRequestDto validTransferWalletRequest;
    private UPITransferRequestDTO validTransferUPIRequest;
    private RazorpayTransactionResponseDTO failureResponse;
    private RazorpayTransactionResponseDTO successResponse;

    public static final String TRANSACTION_SUCCESSFUL = "Transaction Successful";
    public static final String INSUFFICIENT_FUNDS = "Insufficient Funds";
    public static final double AMOUNT = 1000.00;
    public static final String TRANSACTION_ID1 = "transaction1";
    public static final String SENDER_UPI = "sender@upi";



    @BeforeEach
    void setUp() {
        // Prepare a valid transfer request
        validTransferWalletRequest = new WalletTransferRequestDto(
                TRANSACTION_ID1,
                456L,
                789L,
                AMOUNT
        );
        // Prepare a mock payment gateway response
        successResponse = new RazorpayTransactionResponseDTO(
                true,
                TRANSACTION_SUCCESSFUL,
                TRANSACTION_ID1,
                AMOUNT,
                        LocalDateTime.now()
        );

        failureResponse = new RazorpayTransactionResponseDTO(
                false,
                INSUFFICIENT_FUNDS,
                TRANSACTION_ID1,
                AMOUNT,
                LocalDateTime.now()
        );

        validTransferUPIRequest = new UPITransferRequestDTO(
                TRANSACTION_ID1,
                SENDER_UPI,
                "receiver@upi",
                AMOUNT,
                1234
        );

    }


    @Test
    void transferWalletMoneyValidTransferReturnsSuccessfulResponse() {
        // Arrange
        when(razorpay.transferWalletMoney(
                validTransferWalletRequest.transactionId(),
                validTransferWalletRequest.fromWalletId(),
                validTransferWalletRequest.toWalletId(),
                validTransferWalletRequest.amount()
        )).thenReturn(successResponse);

        // Act
        TransactionResponseDto response = razorpayAdapter.transferWalletMoney(validTransferWalletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TRANSACTION_ID1, response.transactionId());
        assertEquals(TransactionStatus.SUCCESS, response.status());
        assertEquals(AMOUNT, response.amount());

        // Verify interaction
        verify(razorpay).transferWalletMoney(
                validTransferWalletRequest.transactionId(),
                validTransferWalletRequest.fromWalletId(),
                validTransferWalletRequest.toWalletId(),
                validTransferWalletRequest.amount()
        );
    }

    @Test
    void transferWalletMoneyRazorpayFailureResponseHandlesError() {
        // Arrange
        RazorpayTransactionResponseDTO failedResponse = new RazorpayTransactionResponseDTO(
                false,
                INSUFFICIENT_FUNDS,
                TRANSACTION_ID1,
                AMOUNT,
                LocalDateTime.now()
        );

        when(razorpay.transferWalletMoney(
                validTransferWalletRequest.transactionId(),
                validTransferWalletRequest.fromWalletId(),
                validTransferWalletRequest.toWalletId(),
                validTransferWalletRequest.amount()
        )).thenReturn(failedResponse);

        // Act
        TransactionResponseDto response = razorpayAdapter.transferWalletMoney(validTransferWalletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.FAILED, response.status());
        assertEquals(INSUFFICIENT_FUNDS, response.remarks());
    }

    // method change

    @Test
    void transferUPIMoneyValidTransferReturnsSuccessfulResponse() {
        // Arrange
        when(razorpay.transferUPIMoney(
                validTransferUPIRequest.transactionId(),
                validTransferUPIRequest.fromUpiId(),
                validTransferUPIRequest.toUpiId(),
                validTransferUPIRequest.pin(),
                validTransferUPIRequest.amount()
        )).thenReturn(successResponse);

        // Act
        TransactionResponseDto response = razorpayAdapter.transferUPIMoney(validTransferUPIRequest);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, response.status());
        assertEquals(TRANSACTION_ID1, response.transactionId());
        assertEquals(AMOUNT, response.amount());
        assertEquals(TRANSACTION_SUCCESSFUL, response.remarks());

        // Verify interaction
        verify(razorpay).transferUPIMoney(
                validTransferUPIRequest.transactionId(),
                validTransferUPIRequest.fromUpiId(),
                validTransferUPIRequest.toUpiId(),
                validTransferUPIRequest.pin(),
                validTransferUPIRequest.amount()
        );
    }

    @Test
    void transferUPIMoneyFailedTransferReturnsFailureResponse() {
        // Arrange
        when(razorpay.transferUPIMoney(
                validTransferUPIRequest.transactionId(),
                validTransferUPIRequest.fromUpiId(),
                validTransferUPIRequest.toUpiId(),
                validTransferUPIRequest.pin(),
                validTransferUPIRequest.amount()
        )).thenReturn(failureResponse);

        // Act
        TransactionResponseDto response = razorpayAdapter.transferUPIMoney(validTransferUPIRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.FAILED, response.status());
        assertEquals(INSUFFICIENT_FUNDS, response.remarks());
        assertEquals(TRANSACTION_ID1, response.transactionId());
        assertEquals(AMOUNT, response.amount());
    }

    @Test
    void validateUPIValidUPIIdReturnsValidResponse() {
        // Arrange
        String validUpiId = SENDER_UPI;
        when(razorpay.validateUPI(validUpiId)).thenReturn(true);

        // Act
        ValidationResponseDTO response = razorpayAdapter.validateUPI(validUpiId);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsValid());

        // Verify interaction
        verify(razorpay).validateUPI(validUpiId);
    }

    @Test
    void validateUPIInvalidUPIIdReturnsInvalidResponse() {
        // Arrange
        String invalidUpiId = SENDER_UPI;
        when(razorpay.validateUPI(invalidUpiId)).thenReturn(false);

        // Act
        ValidationResponseDTO response = razorpayAdapter.validateUPI(invalidUpiId);

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsValid());

        // Verify interaction
        verify(razorpay).validateUPI(invalidUpiId);
    }


    @Test
    void getUPIBalanceSuccessfully() {
        // Arrange
        when(razorpay.checkUPIBalance(SENDER_UPI, 1234)).thenReturn(new BigDecimal(AMOUNT));

        // Act
        BalanceResponseDTO response = razorpayAdapter.checkUPIBalance(SENDER_UPI, 1234);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal(AMOUNT), response.getBalance());

        // Verify interaction
        verify(razorpay).checkUPIBalance(SENDER_UPI, 1234);
    }

    @Test
    void getWalletBalanceSuccessfully() {
        // Arrange
        String walletId = "1L";
        when(razorpay.checkWalletBalance(walletId)).thenReturn(new BigDecimal(AMOUNT));

        // Act
        BalanceResponseDTO response = razorpayAdapter.checkWalletBalance(walletId);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal(AMOUNT), response.getBalance());

        // Verify interaction
        verify(razorpay).checkWalletBalance(walletId);
    }

    @Test
    void transferNEFTMoney_PartiallyFailedTransfers_HandlesErrorScenario() {
        // Arrange
        List<RazorpayTransactionResponseDTO> mixedResponses = new ArrayList<>();
        mixedResponses.add(new RazorpayTransactionResponseDTO(
                true,
                TRANSACTION_SUCCESSFUL,
                TRANSACTION_ID1,
                AMOUNT,
                LocalDateTime.now()
        ));
        mixedResponses.add(new RazorpayTransactionResponseDTO(
                false,
                INSUFFICIENT_FUNDS,
                "transaction2",
                AMOUNT,
                LocalDateTime.now()
        ));

        String receiverUpi = "receiver1@bank";

        List<NeftTransferRequestDto> validNeftTransferRequests;

        validNeftTransferRequests = new ArrayList<>();
        validNeftTransferRequests.add(new NeftTransferRequestDto(
                TRANSACTION_ID1,
                SENDER_UPI,
                receiverUpi,
                "BANK001",
                "BANK002",
                AMOUNT
        ));
        validNeftTransferRequests.add(new NeftTransferRequestDto(
                "transaction2",
                SENDER_UPI,
                receiverUpi,
                "BANK002",
                "BANK001",
                AMOUNT
        ));

        when(razorpay.transferNEFTMoney(validNeftTransferRequests))
                .thenReturn(mixedResponses);
        // Act
        List<TransactionResponseDto> responses = razorpayAdapter.transferNEFTMoney(validNeftTransferRequests);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        // Verify first transaction (successful)
        assertEquals(TransactionStatus.SUCCESS, responses.get(0).status());
        assertEquals(TRANSACTION_ID1, responses.get(0).transactionId());

        // Verify second transaction (failed)
        assertNotEquals(TransactionStatus.SUCCESS, responses.get(1).status());
        assertEquals("transaction2", responses.get(1).transactionId());
        assertEquals(INSUFFICIENT_FUNDS, responses.get(1).remarks());
    }
}
