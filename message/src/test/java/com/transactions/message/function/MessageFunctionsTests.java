package com.transactions.message.function;

import com.transactions.message.clients.UsersServiceFeignClient;
import com.transactions.message.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageFunctionsTests {

    @Mock
    private UsersServiceFeignClient usersServiceFeignClient;

    @Mock
    private Logger mockLogger;

    private MessageFunctions messageFunctions;

    private UserResponseDTO userResponseDTO;
    private UserServiceResponseDTO userServiceResponseDTO;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO(
                123L,
                "John Doe",
                "user@example.com",
                "1234567890",
                AccountStatusEnum.ACTIVE
        );
        userServiceResponseDTO = new UserServiceResponseDTO(
                new StatusModel(200, "OK"),
                "User retrieved successfully",
                userResponseDTO
        );
        messageFunctions = new MessageFunctions(usersServiceFeignClient);
    }

    @Test
    void testEmailSuccessfulSend() {
        // Arrange
        MessageInfoDto messageInfoDto = new MessageInfoDto("123", "Test Message");

        userServiceResponseDTO.setResponse(userResponseDTO);

        when(usersServiceFeignClient.getUser(123L)).thenReturn(userServiceResponseDTO);

        // Act
        Function<MessageInfoDto, MessageInfoDto> emailFunction = messageFunctions.email();
        MessageInfoDto result = emailFunction.apply(messageInfoDto);

        // Assert
        assertNotNull(result);
        assertEquals(messageInfoDto, result);
        verify(usersServiceFeignClient).getUser(123L);
    }

    @Test
    void testSmsSuccessfulSend() {
        // Arrange
        MessageInfoDto messageInfoDto = new MessageInfoDto("123", "Test Message");

        when(usersServiceFeignClient.getUser(123L)).thenReturn(userServiceResponseDTO);

        // Act
        Function<MessageInfoDto, MessageInfoDto> smsFunction = messageFunctions.sms();
        MessageInfoDto result = smsFunction.apply(messageInfoDto);

        // Assert
        assertNotNull(result);
        assertEquals(messageInfoDto, result);
        verify(usersServiceFeignClient).getUser(123L);
    }

    @Test
    void testEmailUserFetchFailure() {
        // Arrange
        MessageInfoDto messageInfoDto = new MessageInfoDto("123", "Test Message");

        when(usersServiceFeignClient.getUser(123L))
                .thenThrow(new RuntimeException("User service unavailable"));

        // Act
        Function<MessageInfoDto, MessageInfoDto> emailFunction = messageFunctions.email();
        MessageInfoDto result = emailFunction.apply(messageInfoDto);

        // Assert
        assertNotNull(result);
        assertEquals(messageInfoDto, result);
        verify(usersServiceFeignClient).getUser(123L);
    }

    @Test
    void testSmsUserFetchFailure() {
        // Arrange
        MessageInfoDto messageInfoDto = new MessageInfoDto("123", "Test Message");

        when(usersServiceFeignClient.getUser(123L))
                .thenThrow(new RuntimeException("User service unavailable"));

        // Act
        Function<MessageInfoDto, MessageInfoDto> smsFunction = messageFunctions.sms();
        MessageInfoDto result = smsFunction.apply(messageInfoDto);

        // Assert
        assertNotNull(result);
        assertEquals(messageInfoDto, result);
        verify(usersServiceFeignClient).getUser(123L);
    }
}
