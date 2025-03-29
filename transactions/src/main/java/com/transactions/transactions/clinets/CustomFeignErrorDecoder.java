package com.transactions.transactions.clinets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dto.response.ConnectorApiErrorResponse;
import com.transactions.transactions.exception.ApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Read the error response body
            String errorBody = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);

            // Parse the JSON error response into your error model
            ConnectorApiErrorResponse errorResponse = objectMapper.readValue(errorBody, ConnectorApiErrorResponse.class);

            // Create a custom exception with the parsed error details
            return new ApiException(
                    errorResponse.getStatusModel().getStatusCode(),
                    errorResponse.getResponseMsg(),
                    errorResponse.getStatusModel().getStatusMsg()
            );
        } catch (IOException e) {
            // Fallback if parsing fails
            return new ApiException(response.status(), "Error processing response", "UNKNOWN");
        }
    }
}