package com.transactions.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.dtos.ValidationRequestDto;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.exceptions.GlobalExceptionHandler;
import com.transactions.users.exceptions.UserNotFoundException;
import com.transactions.users.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    UserService userService;

    @InjectMocks
    private UsersController usersController;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(usersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }



    private static final String BASE_URL = "/api/v1"; // Assuming endpoint starts with "/users"

    @Test
    void testGetUser_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserResponseDTO mockUser = new UserResponseDTO(
                userId, "John Doe", "john.doe@example.com", "1234567890", AccountStatusEnum.ACTIVE
        );

        when(userService.getUserDetails(userId)).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService, times(1)).getUserDetails(userId);
    }

    @Test
    public void testGetUser_InvalidId_ThrowsException() throws Exception {
        // Arrange
        Long invalidUserId = 2L;

        // Mock the service to throw an exception
        when(userService.getUserDetails(invalidUserId))
                .thenThrow(new UserNotFoundException(String.format("User with id %d Not Found", invalidUserId)));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", invalidUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testValidateUser_ValidCredentials_ReturnsTrue() throws Exception {
        // Arrange
        ValidationRequestDto validationRequest = new ValidationRequestDto(1L, "password123");

        // Mock the service method
        when(userService.validateUser(validationRequest)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Verify that the service method was called exactly once
        verify(userService, times(1)).validateUser(validationRequest);
    }

    @Test
    public void testValidateUser_InvalidCredentials_ReturnsFalse() throws Exception {
        // Arrange
        ValidationRequestDto validationRequest = new ValidationRequestDto(1L, "wrongpassword");

        // Mock the service method
        when(userService.validateUser(validationRequest)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Verify that the service method was called exactly once
        verify(userService, times(1)).validateUser(validationRequest);
    }

    @Test
    public void testValidateUser_InvalidRequest_ThrowsValidationError() throws Exception {
        // Arrange
        ValidationRequestDto invalidRequest = new ValidationRequestDto(null, "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}