package com.transactions.message.clients;

import com.transactions.message.dto.AccountStatusEnum;
import com.transactions.message.dto.StatusModel;
import com.transactions.message.dto.UserResponseDTO;
import com.transactions.message.dto.UserServiceResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceFeignClientTests {
    @Mock
    private UsersServiceFeignClient usersServiceFeignClient;

    private UserServiceResponseDTO successResponse;
    private UserServiceResponseDTO failureResponse;

    @BeforeEach
    void setUp() {
        // Success scenario response
        StatusModel successStatus = new StatusModel(200, "OK");
        UserResponseDTO userResponse = new UserResponseDTO(
                1L,
                "johndoe",
                "john.doe@example.com",
                "9876543210",
                AccountStatusEnum.ACTIVE
        );

        successResponse = new UserServiceResponseDTO(successStatus, "User retrieved successfully", userResponse);

        // Failure scenario response
        StatusModel failureStatus = new StatusModel(404, "Not Found");
        failureResponse = new UserServiceResponseDTO(failureStatus, "User not found", null);
    }

    @Test
    void testGetUserValidIdReturnsSuccessfulResponse() {
        // Arrange
        Long validUserId = 1L;
        when(usersServiceFeignClient.getUser(validUserId)).thenReturn(successResponse);

        // Act
        UserServiceResponseDTO response = usersServiceFeignClient.getUser(validUserId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusModel().getStatusCode());
        assertEquals("OK", response.getStatusModel().getStatusMsg());
        assertEquals("User retrieved successfully", response.getResponseMsg());
        assertNotNull(response.getResponse());
        assertEquals(1L, response.getResponse().id());

        // Verify interaction
        verify(usersServiceFeignClient).getUser(validUserId);
    }

    @Test
    void testGetUserNonExistentIdReturnsNotFoundResponse() {
        // Arrange
        Long nonExistentUserId = 999L;
        when(usersServiceFeignClient.getUser(nonExistentUserId)).thenReturn(failureResponse);

        // Act
        UserServiceResponseDTO response = usersServiceFeignClient.getUser(nonExistentUserId);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusModel().getStatusCode());
        assertEquals("Not Found", response.getStatusModel().getStatusMsg());
        assertEquals("User not found", response.getResponseMsg());
        assertNull(response.getResponse());

        // Verify interaction
        verify(usersServiceFeignClient).getUser(nonExistentUserId);
    }
}
