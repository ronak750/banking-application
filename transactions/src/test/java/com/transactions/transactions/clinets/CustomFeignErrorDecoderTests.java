package com.transactions.transactions.clinets;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.response.ConnectorApiErrorResponse;
import com.transactions.transactions.exception.ApiException;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomFeignErrorDecoderTests {

    // Global constants
    private static final String TEST_METHOD_KEY = "testMethod";
    private static final String ERROR_PROCESSING_MESSAGE = "Error processing response";
    private static final String UNKNOWN_STATUS = "UNKNOWN";
    private static final String INVALID_INPUT_MESSAGE = "Invalid input provided";
    private static final String BAD_REQUEST_STATUS = "Bad Request";
    private static final String MALFORMED_JSON = "{invalid json}";
    private static final String EMPTY_JSON = "";

    private CustomFeignErrorDecoder errorDecoder;

    @Mock
    private Response response;

    @Mock
    private Response.Body responseBody;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        errorDecoder = new CustomFeignErrorDecoder();
        objectMapper = new ObjectMapper();
    }

    @Test
    void decodeValidErrorResponse() throws IOException {
        // Arrange
        ConnectorApiErrorResponse errorResponse = new ConnectorApiErrorResponse();
        StatusModel statusModel = StatusModel.builder()
                .statusCode(400)
                .statusMsg(BAD_REQUEST_STATUS)
                .build();
        errorResponse.setStatusModel(statusModel);
        errorResponse.setResponseMsg(INVALID_INPUT_MESSAGE);

        // Convert error response to JSON
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        // Mock response body
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream())
                .thenReturn(new ByteArrayInputStream(errorJson.getBytes(StandardCharsets.UTF_8)));

        // Act
        Exception result = errorDecoder.decode(TEST_METHOD_KEY, response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(ApiException.class, result);

        ApiException apiException = (ApiException) result;
        assertEquals(400, apiException.getStatusCode());
        assertEquals(INVALID_INPUT_MESSAGE, apiException.getErrorMessage());
        assertEquals(BAD_REQUEST_STATUS, apiException.getErrorCode());
    }

    @Test
    void decodeMalformedJsonResponse() throws IOException {
        // Arrange
        // Mock response body
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream())
                .thenReturn(new ByteArrayInputStream(MALFORMED_JSON.getBytes(StandardCharsets.UTF_8)));
        when(response.status()).thenReturn(500);

        // Act
        Exception result = errorDecoder.decode(TEST_METHOD_KEY, response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(ApiException.class, result);

        ApiException apiException = (ApiException) result;
        assertEquals(500, apiException.getStatusCode());
        assertEquals(ERROR_PROCESSING_MESSAGE, apiException.getErrorMessage());
        assertEquals(UNKNOWN_STATUS, apiException.getErrorCode());
    }

    @Test
    void decodeEmptyResponseBody() throws IOException {
        // Arrange
        // Mock response body
        when(response.body()).thenReturn(responseBody);
        when(responseBody.asInputStream())
                .thenReturn(new ByteArrayInputStream(EMPTY_JSON.getBytes(StandardCharsets.UTF_8)));
        when(response.status()).thenReturn(500);

        // Act
        Exception result = errorDecoder.decode(TEST_METHOD_KEY, response);

        // Assert
        assertNotNull(result);
        assertInstanceOf(ApiException.class, result);

        ApiException apiException = (ApiException) result;
        assertEquals(500, apiException.getStatusCode());
        assertEquals(ERROR_PROCESSING_MESSAGE, apiException.getErrorMessage());
        assertEquals(UNKNOWN_STATUS, apiException.getErrorCode());
    }
}