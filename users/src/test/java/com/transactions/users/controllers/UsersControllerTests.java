package com.transactions.users.controllers;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.dtos.ValidationRequestDto;
import com.transactions.users.dtos.commons.SuccessResponseDTO;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UsersController usersController;

    private UserDTO userDTO;
    private UserResponseDTO userResponseDTO;
    private ValidationRequestDto validationRequestDto;

    @BeforeEach
    void setUp() {
        // Setup common test data
        userDTO = new UserDTO("John Doe", "john@example.com", "1234567890", "password123");

        userResponseDTO = new UserResponseDTO(1L, "John Doe", "john@example.com", "1234567890", AccountStatusEnum.ACTIVE);

        validationRequestDto = new ValidationRequestDto(1L, "password123");
    }

    @Test
    void testCreateUser_Successful() {
        // Arrange
        String headerMobileNumber = "1234567890";
        when(userService.saveUser(userDTO, headerMobileNumber)).thenReturn(userResponseDTO);

        // Act
        ResponseEntity<SuccessResponseDTO<UserResponseDTO>> response =
                usersController.createUser(userDTO, headerMobileNumber);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User Creation Successful", response.getBody().getMessage());
        assertEquals(userResponseDTO, response.getBody().getData());

        // Verify service method was called
        verify(userService).saveUser(userDTO, headerMobileNumber);
    }

    @Test
    void testGetUser_Successful() {
        // Arrange
        Long userId = 1L;
        when(userService.getUserDetails(userId)).thenReturn(userResponseDTO);

        // Act
        ResponseEntity<SuccessResponseDTO<UserResponseDTO>> response =
                usersController.getUser(userId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User Creation Successful", response.getBody().getMessage());
        assertEquals(userResponseDTO, response.getBody().getData());

        // Verify service method was called
        verify(userService).getUserDetails(userId);
    }

    @Test
    void testValidateUser_Successful() {
        // Arrange
        when(userService.validateUser(validationRequestDto)).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponseDTO<Boolean>> response =
                usersController.validateUser(validationRequestDto);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User Validation Status Fetched Successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData());

        // Verify service method was called
        verify(userService).validateUser(validationRequestDto);
    }

}
