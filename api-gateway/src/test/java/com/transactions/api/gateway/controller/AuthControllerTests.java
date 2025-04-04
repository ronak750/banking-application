package com.transactions.api.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.api.gateway.dto.APITokenResponseDTO;
import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.dto.commons.StatusModel;
import com.transactions.api.gateway.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.transactions.api.gateway.constant.Constants.INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTests {

    public static final String AUTH_LOGIN = "/auth/login";
    private WebTestClient webTestClient;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        webTestClient = WebTestClient
                .bindToController(authController)
                .build();
    }
//
//    @Test
//    public void testLoginSuccessfulAuthentication() {
//        // Arrange
//        LoginDto loginDto = new LoginDto(1L, "password");
//
//        var successResponse = APIResponseDTO.builder()
//                .response(new APITokenResponseDTO("valid-jwt-token"))
//                .statusModel(new StatusModel(200, "SUCCESS"))
//                .build();
//
//        // Mock the service method to return a successful response
//        when(authService.login(any(LoginDto.class)))
//                .thenReturn(Mono.just(ResponseEntity.ok(successResponse)));
//
//        // Act & Assert
//        webTestClient.post()
//                .uri("/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(loginDto)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.token").isEqualTo("valid-jwt-token");
//    }
//
//    @Test
//    public void testLoginUnauthorizedAuthentication() {
//        // Arrange
//        LoginDto loginDto = new LoginDto(-1L, "wrongpassword");
//
//        // Mock the service method to return an unauthorized response
//        when(authService.login(any(LoginDto.class)))
//                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
//
//        // Act & Assert
//        webTestClient.post()
//                .uri("/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(loginDto)
//                .exchange()
//                .expectStatus().isUnauthorized();
//    }
//
//    @Test
//    public void testLoginInvalidCredentials() {
//        // Arrange
//        LoginDto invalidLoginDto = new LoginDto(1L, "");
//
//        // Mock the service method to handle invalid credentials
//        when(authService.login(any(LoginDto.class)))
//                .thenReturn(Mono.just(ResponseEntity.badRequest().build()));
//
//        // Act & Assert
//        webTestClient.post()
//                .uri("/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(invalidLoginDto)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }

    private LoginDto validLoginDto;
    private LoginDto invalidLoginDto;

    @BeforeEach
    void setUp() {
        validLoginDto = new LoginDto(1L, "validpassword");

        invalidLoginDto = new LoginDto(2L, "invalidpassword");
    }


    @Test
    void testSuccessfulLogin() {
        // Prepare successful login response
        APIResponseDTO successResponse = APIResponseDTO.builder()
                .responseMsg("Login successful")
                .statusModel(new StatusModel(200, "SUCCESS"))
                .response(new APITokenResponseDTO("sample-jwt-token"))
                .build();

        // Stub the service method
        when(authService.login(validLoginDto))
                .thenReturn(Mono.just(ResponseEntity.ok(successResponse)));

        ObjectMapper objectMapper = new ObjectMapper();
//        APIToken apiToken = objectMapper.readValue(json, APIToken.class);

        // Perform test
        webTestClient.post()
                .uri(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validLoginDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody(APIResponseDTO.class)
                .consumeWith(response -> {
                    APIResponseDTO responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals("Login successful", responseBody.getResponseMsg());
                    assertEquals(200, responseBody.getStatusModel().getStatusCode());
                    assertEquals("SUCCESS", responseBody.getStatusModel().getStatusMsg());

                    assertTrue(responseBody.getResponse() instanceof Map<?, ?>);
                    Map<String, String> tokenResponse = (Map<String, String>) responseBody.getResponse();
                    assertTrue(tokenResponse.containsKey("token"));
                    assertEquals("sample-jwt-token", tokenResponse.get("token"));
                });

        // Verify service method was called
        verify(authService, times(1)).login(validLoginDto);
    }

    @Test
    void testUnauthorizedLogin() {
        // Prepare unauthorized login response
        APIResponseDTO unauthorizedResponse = APIResponseDTO.builder()
                .responseMsg("Invalid credentials or user is blocked")
                .statusModel(new StatusModel(401, INVALID_CREDENTIALS))
                .build();

        // Stub the service method
        when(authService.login(invalidLoginDto))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedResponse)));

        // Perform test
        webTestClient.post()
                .uri(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalidLoginDto))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(APIResponseDTO.class)
                .consumeWith(response -> {
                    APIResponseDTO responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals("Invalid credentials or user is blocked", responseBody.getResponseMsg());
                    assertEquals(401, responseBody.getStatusModel().getStatusCode());
                    assertEquals(INVALID_CREDENTIALS, responseBody.getStatusModel().getStatusMsg());
                });

        // Verify service method was called
        verify(authService, times(1)).login(invalidLoginDto);
    }

    @Test
    void testServiceUnavailableLogin() {
        // Prepare service unavailable response
        APIResponseDTO serviceUnavailableResponse = APIResponseDTO.builder()
                .responseMsg("Could not verify your details. Please try after some time")
                .statusModel(new StatusModel(503, "SERVICE_UNAVAILABLE"))
                .build();

        // Stub the service method
        when(authService.login(any(LoginDto.class)))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(serviceUnavailableResponse)));

        // Perform test
        webTestClient.post()
                .uri(AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validLoginDto))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(APIResponseDTO.class)
                .consumeWith(response -> {
                    APIResponseDTO responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals("Could not verify your details. Please try after some time", responseBody.getResponseMsg());
                    assertEquals(503, responseBody.getStatusModel().getStatusCode());
                    assertEquals("SERVICE_UNAVAILABLE", responseBody.getStatusModel().getStatusMsg());
                });

        // Verify service method was called
        verify(authService, times(1)).login(any(LoginDto.class));
    }

}