package com.transactions.transactions.clinets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dto.request.*;
import com.transactions.transactions.exception.ApiException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.function.Executable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.transactions.transactions.constant.Constants.TRANSACTION_REQUEST_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayConnectorFallbackFactoryTests {

    private static final String DUMMY_ERROR_MESSAGE = "Test error message";
    private static final String DUMMY_UPI_ID = "test@upi.com";
    private static final Long DUMMY_WALLET_ID = 12345L;
    private static final String DUMMY_WALLETID_STRING = "12345L";
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final String DUMMY_IFSC_CODE = "BANK0001234";
    private static final String DUMMY_ACCOUNT_NUMBER = "1234567890";

    @Mock
    private ObjectMapper objectMapper;

    private GatewayConnectorFallbackFactory fallbackFactory;

    @BeforeEach
    void setup() {
        fallbackFactory = new GatewayConnectorFallbackFactory();
    }

    @Test
    void testCreateWhenFeignExceptionWith400Status(){
        // Arrange
        String errorBody = "{\"responseMsg\":\"Bad Request\"}";
        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.status()).thenReturn(HTTP_BAD_REQUEST);
        when(mockFeignException.contentUTF8()).thenReturn(errorBody);

        // Act
        GatewayConnectorFeignClient feignClient = fallbackFactory.create(mockFeignException);

        // Assert tests for different methods
        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferWalletMoney(createDummyWalletTransferRequest()),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferUpiMoney(createDummyUpiTransferRequest()),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferNEFTMoney(createDummyNeftTransferRequestList()),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.checkUPIBalance(createDummyUpiBalanceCheckRequest()),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.validateUPI(DUMMY_UPI_ID),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.checkWalletBalance(DUMMY_WALLETID_STRING),
                HTTP_BAD_REQUEST,
                "Bad Request",
                TRANSACTION_REQUEST_FAILED
        );
    }

    @Test
    void testCreateWhenFeignExceptionWithNon400Status() {
        // Arrange
        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.status()).thenReturn(HTTP_INTERNAL_SERVER_ERROR);
        when(mockFeignException.getMessage()).thenReturn(DUMMY_ERROR_MESSAGE);

        // Act
        GatewayConnectorFeignClient feignClient = fallbackFactory.create(mockFeignException);

        // Assert tests for different methods
        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferWalletMoney(createDummyWalletTransferRequest()),
                HTTP_INTERNAL_SERVER_ERROR,
                DUMMY_ERROR_MESSAGE,
                "UNKNOWN"
        );

        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferUpiMoney(createDummyUpiTransferRequest()),
                HTTP_INTERNAL_SERVER_ERROR,
                DUMMY_ERROR_MESSAGE,
                "UNKNOWN"
        );
    }

    @Test
    void testCreateWhenGenericThrowable() {
        // Arrange
        Throwable genericThrowable = new RuntimeException(DUMMY_ERROR_MESSAGE);

        // Act
        GatewayConnectorFeignClient feignClient = fallbackFactory.create(genericThrowable);

        // Assert
        assertThrowsApiExceptionWithDetails(
                () -> feignClient.transferWalletMoney(createDummyWalletTransferRequest()),
                HTTP_INTERNAL_SERVER_ERROR,
                "Error processing response",
                "UNKNOWN"
        );
    }

    private void assertThrowsApiExceptionWithDetails(
            Executable executable,
            int expectedStatus,
            String expectedMessage,
            String expectedCode
    ) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(expectedStatus, exception.getStatusCode());
        assertEquals(expectedMessage, exception.getErrorMessage());
        assertEquals(expectedCode, exception.getErrorCode());
    }

    // Dummy request creation methods
    private WalletTransferRequestDto createDummyWalletTransferRequest() {
        return new WalletTransferRequestDto(
                UUID.randomUUID().toString(),
                DUMMY_WALLET_ID,
                DUMMY_WALLET_ID + 1,
                1000.0
        );
    }

    private UPITransferRequestDTO createDummyUpiTransferRequest() {
        return new UPITransferRequestDTO(
                UUID.randomUUID().toString(),
                DUMMY_UPI_ID,
                "another" + DUMMY_UPI_ID,
                1000.0,
                1234
        );
    }

    private List<NEFTtransferRequestDto> createDummyNeftTransferRequestList() {
        List<NEFTtransferRequestDto> requests = new ArrayList<>();
        requests.add(new NEFTtransferRequestDto(
                UUID.randomUUID().toString(),
                DUMMY_ACCOUNT_NUMBER,
                DUMMY_ACCOUNT_NUMBER + "1",
                DUMMY_IFSC_CODE,
                DUMMY_IFSC_CODE + "1",
                1000.0
        ));
        return requests;
    }

    private UPIBalanceCheckRequestDto createDummyUpiBalanceCheckRequest() {
        return new UPIBalanceCheckRequestDto(
                DUMMY_UPI_ID,
                1234
        );
    }

}