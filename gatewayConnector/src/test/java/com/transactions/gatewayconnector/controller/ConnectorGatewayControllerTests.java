package com.transactions.gatewayconnector.controller;

import com.transactions.gatewayconnector.dto.*;
import com.transactions.gatewayconnector.dto.request.*;
import com.transactions.gatewayconnector.dto.response.BalanceResponseDTO;
import com.transactions.gatewayconnector.dto.response.TransactionResponseDto;
import com.transactions.gatewayconnector.dto.response.ValidationResponseDTO;
import com.transactions.gatewayconnector.service.IAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.transactions.gatewayconnector.constant.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectorGatewayControllerTests {

    @Mock
    private IAdapter adapter;

    @InjectMocks
    private ConnectorGatewayController controller;

    // Wallet Transfer Test Cases
    @Test
    void testTransferWalletMoneySuccessful() {
        // Arrange
        WalletTransferRequestDto requestDto = new WalletTransferRequestDto(
                "WALLET_TXN_001", 12345L, 67890L, 500.00
        );
        TransactionResponseDto expectedResponse = new TransactionResponseDto(
                "WALLET_TXN_001", 500.00, TransactionStatus.SUCCESS, "Wallet transfer successful", null
        );

        when(adapter.transferWalletMoney(requestDto)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.transferWalletMoney(requestDto);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(TRANSACTION_SUCCESSFUL, apiResponse.getResponseMsg());
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());

        TransactionResponseDto actualResponse = (TransactionResponseDto) apiResponse.getResponse();
        assertEquals(expectedResponse, actualResponse);

        verify(adapter).transferWalletMoney(requestDto);
    }

    // UPI Transfer Test Cases
    @Test
    void testTransferUpiMoneySuccessful() {
        // Arrange
        UPITransferRequestDTO requestDto = new UPITransferRequestDTO(
                "UPI_TXN_001", "sender@upi", "receiver@upi", 1000.00, 1234
        );
        TransactionResponseDto expectedResponse = new TransactionResponseDto(
                "UPI_TXN_001", 1000.00, TransactionStatus.SUCCESS, "UPI transfer successful", null
        );

        when(adapter.transferUPIMoney(requestDto)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.transferUpiMoney(requestDto);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(BALANCE_SUCCESSFULLY_FETCHED, apiResponse.getResponseMsg());
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());

        TransactionResponseDto actualResponse = (TransactionResponseDto) apiResponse.getResponse();
        assertEquals(expectedResponse, actualResponse);

        verify(adapter).transferUPIMoney(requestDto);
    }

    // NEFT Transfer Test Cases
    @Test
    void testTransferNEFTMoneySuccessful() {
        // Arrange
        List<NeftTransferRequestDto> requestDtoList = Arrays.asList(
                new NeftTransferRequestDto(
                        "NEFT_TXN_001", "123456789", "987654321",
                        "BANK1IFSC", "BANK2IFSC", 5000.00
                )
        );
        TransactionResponseDto expectedResponse = new TransactionResponseDto(
                "NEFT_TXN_001", 5000.00, TransactionStatus.SUCCESS, "NEFT transfer successful", null
        );

        when(adapter.transferNEFTMoney(requestDtoList)).thenReturn(List.of(expectedResponse));

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.transferNEFTMoney(requestDtoList);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());


        verify(adapter).transferNEFTMoney(requestDtoList);
    }

    // UPI Balance Test Cases
    @Test
    void testCheckUPIBalanceSuccessful() {
        // Arrange
        UPIAndPINRequestDto requestDto = new UPIAndPINRequestDto("test@upi", 1234);
        BalanceResponseDTO expectedResponse = new BalanceResponseDTO(new BigDecimal("5000.00"));

        when(adapter.checkUPIBalance(requestDto.upiId(), requestDto.pin())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.checkUPIBalance(requestDto);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(BALANCE_SUCCESSFULLY_FETCHED, apiResponse.getResponseMsg());
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());

        BalanceResponseDTO actualResponse = (BalanceResponseDTO) apiResponse.getResponse();
        assertEquals(expectedResponse.getBalance(), actualResponse.getBalance());

        verify(adapter).checkUPIBalance(requestDto.upiId(), requestDto.pin());
    }

    // UPI Validation Test Cases
    @Test
    void testValidateUPISuccessful() {
        // Arrange
        String upiId = "test@upi";
        ValidationResponseDTO expectedResponse = new ValidationResponseDTO(true);

        when(adapter.validateUPI(upiId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.validateUPI(upiId);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(BALANCE_SUCCESSFULLY_FETCHED, apiResponse.getResponseMsg());
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());

        ValidationResponseDTO actualResponse = (ValidationResponseDTO) apiResponse.getResponse();
        assertEquals(expectedResponse.getIsValid(), actualResponse.getIsValid());

        verify(adapter).validateUPI(upiId);
    }

    // Wallet Balance Test Cases
    @Test
    void testCheckWalletBalanceSuccessful() {
        // Arrange
        String walletId = "WALLET_123";
        BalanceResponseDTO expectedResponse = new BalanceResponseDTO(new BigDecimal("2500.00"));

        when(adapter.checkWalletBalance(walletId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<APIResponseDTO> responseEntity = controller.checkWalletBalance(walletId);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());

        APIResponseDTO apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(BALANCE_SUCCESSFULLY_FETCHED, apiResponse.getResponseMsg());
        assertEquals(200, apiResponse.getStatusModel().getStatusCode());
        assertEquals(SUCCESS, apiResponse.getStatusModel().getStatusMsg());

        BalanceResponseDTO actualResponse = (BalanceResponseDTO) apiResponse.getResponse();
        assertEquals(expectedResponse.getBalance(), actualResponse.getBalance());

        verify(adapter).checkWalletBalance(walletId);
    }
}