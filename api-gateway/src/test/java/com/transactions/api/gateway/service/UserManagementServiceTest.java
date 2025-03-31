package com.transactions.api.gateway.service;

import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserManagementServiceTest {

    private UserManagementService userManagementService;
    private WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private RequestBodyUriSpec requestBodyUriSpec;
    private RequestBodySpec requestBodySpec;
    private RequestHeadersSpec<?> requestHeadersSpec;
    private ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        webClientBuilder = mock(WebClient.Builder.class);
        webClient = mock(WebClient.class);
        requestBodyUriSpec = mock(RequestBodyUriSpec.class);
        requestBodySpec = mock(RequestBodySpec.class);
        requestHeadersSpec = mock(RequestHeadersSpec.class);
        responseSpec = mock(ResponseSpec.class);

        when(webClientBuilder.baseUrl(any(String.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        userManagementService = new UserManagementService(webClientBuilder);

        // Mock WebClient behavior using doReturn to avoid type mismatch
        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(eq("api/v1/validate"));
        doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any(LoginDto.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    @Test
    void testGetUserReturnsTrueForValidResponse() {
        LoginDto loginDto = new LoginDto(12345L, "password"); // userID as Long
        APIResponseDTO apiResponse = new APIResponseDTO();
        apiResponse.setResponse(Map.of("isValid", true));

        doReturn(Mono.just(apiResponse)).when(responseSpec).bodyToMono(APIResponseDTO.class);

        Mono<Boolean> result = userManagementService.getUser(loginDto);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testGetUserReturnsFalseForInvalidResponse() {
        LoginDto loginDto = new LoginDto(12345L, "password"); // userID as Long
        APIResponseDTO apiResponse = new APIResponseDTO();
        apiResponse.setResponse(Map.of("isValid", false));

        doReturn(Mono.just(apiResponse)).when(responseSpec).bodyToMono(APIResponseDTO.class);

        Mono<Boolean> result = userManagementService.getUser(loginDto);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testGetUserReturnsFalseForNullResponse() {
        LoginDto loginDto = new LoginDto(12345L, "password"); // userID as Long
        APIResponseDTO apiResponse = new APIResponseDTO();
        apiResponse.setResponse(null);

        doReturn(Mono.just(apiResponse)).when(responseSpec).bodyToMono(APIResponseDTO.class);

        Mono<Boolean> result = userManagementService.getUser(loginDto);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testGetUserHandlesErrorGracefully() {
        LoginDto loginDto = new LoginDto(12345L, "password"); // userID as Long

        doReturn(Mono.error(new RuntimeException("Service error"))).when(responseSpec).bodyToMono(APIResponseDTO.class);

        Mono<Boolean> result = userManagementService.getUser(loginDto);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}
