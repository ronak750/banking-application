package com.transactions.api.gateway.service;

import com.transactions.api.gateway.dto.APITokenResponseDTO;
import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.dto.commons.StatusModel;
import com.transactions.api.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static com.transactions.api.gateway.constant.Constants.INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private LoginDto validLoginDto;
    private LoginDto invalidLoginDto;

    @BeforeEach
    void setUp() {
        validLoginDto = new LoginDto(1L, "validpassword");

        invalidLoginDto = new LoginDto(2L, "invalidpassword");
    }

    @Test
    void testSuccessfulLogin() {
        // Prepare mocking for successful login scenario
        when(userManagementService.getUser(validLoginDto))
                .thenReturn(Mono.just(true));

        when(jwtUtil.generateToken(validLoginDto.userId().toString()))
                .thenReturn("sample-jwt-token");

        // Execute and verify
        Mono<ResponseEntity<APIResponseDTO>> resultMono = authService.login(validLoginDto);

        StepVerifier.create(resultMono)
                .expectNextMatches(responseEntity -> {
                    // Verify HTTP Status
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

                    // Verify Response DTO
                    APIResponseDTO responseDTO = responseEntity.getBody();
                    assertNotNull(responseDTO);
                    assertEquals("Login successful", responseDTO.getResponseMsg());

                    // Verify Status Model
                    StatusModel statusModel = responseDTO.getStatusModel();
                    assertNotNull(statusModel);
                    assertEquals(200, statusModel.getStatusCode());
                    assertEquals("SUCCESS", statusModel.getStatusMsg());

                    // Verify Token Response
                    assertTrue(responseDTO.getResponse() instanceof APITokenResponseDTO);
                    APITokenResponseDTO tokenResponse = (APITokenResponseDTO) responseDTO.getResponse();
                    assertEquals("sample-jwt-token", tokenResponse.getToken());

                    return true;
                })
                .verifyComplete();

        // Verify interactions
        verify(userManagementService, times(1)).getUser(validLoginDto);
        verify(jwtUtil, times(1)).generateToken(validLoginDto.userId().toString());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // Prepare mocking for invalid login scenario
        when(userManagementService.getUser(invalidLoginDto))
                .thenReturn(Mono.just(false));

        // Execute and verify
        Mono<ResponseEntity<APIResponseDTO>> resultMono = authService.login(invalidLoginDto);

        StepVerifier.create(resultMono)
                .expectNextMatches(responseEntity -> {
                    // Verify HTTP Status
                    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

                    // Verify Response DTO
                    APIResponseDTO responseDTO = responseEntity.getBody();
                    assertNotNull(responseDTO);
                    assertEquals("Invalid credentials or user is blocked", responseDTO.getResponseMsg());

                    // Verify Status Model
                    StatusModel statusModel = responseDTO.getStatusModel();
                    assertNotNull(statusModel);
                    assertEquals(401, statusModel.getStatusCode());
                    assertEquals(INVALID_CREDENTIALS, statusModel.getStatusMsg());

                    // Verify no response object
                    assertNull(responseDTO.getResponse());

                    return true;
                })
                .verifyComplete();

        // Verify interactions
        verify(userManagementService, times(1)).getUser(invalidLoginDto);
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLoginWithUserManagementServiceError() {
        // Simulate an error in user management service
        when(userManagementService.getUser(any(LoginDto.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // Execute and verify
        Mono<ResponseEntity<APIResponseDTO>> resultMono = authService.login(validLoginDto);

        StepVerifier.create(resultMono)
                .expectNextMatches(responseEntity -> {
                    // Verify HTTP Status
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());

                    // Verify Response DTO
                    APIResponseDTO responseDTO = responseEntity.getBody();
                    assertNotNull(responseDTO);
                    assertEquals("Could not verify your details. Please try after some time", responseDTO.getResponseMsg());

                    // Verify Status Model
                    StatusModel statusModel = responseDTO.getStatusModel();
                    assertNotNull(statusModel);
                    assertEquals(503, statusModel.getStatusCode());
                    assertEquals("SERVICE_UNAVAILABLE", statusModel.getStatusMsg());

                    // Verify no response object
                    assertNull(responseDTO.getResponse());

                    return true;
                })
                .verifyComplete();

        // Verify interactions
        verify(userManagementService, times(1)).getUser(any(LoginDto.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }
}