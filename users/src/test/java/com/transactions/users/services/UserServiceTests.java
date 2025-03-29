package com.transactions.users.services;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.dtos.ValidationRequestDto;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.entities.Users;
import com.transactions.users.exceptions.DuplicateUserException;
import com.transactions.users.exceptions.UserNotFoundException;
import com.transactions.users.repos.UserRepo;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testSaveUser_SUCCESS() {
        String mobNo = "1234567890";
        UserDTO userDto = new UserDTO("user_name", "test@example.com", mobNo,"password");
        Users savedUser = new Users(1L, "user_name", "test@example.com", "1234567890", "password", AccountStatusEnum.ACTIVE);

        when(userRepo.findByMobileNumberOrEmail(anyString(), anyString())).thenReturn(null);
        when(userRepo.save(any(Users.class))).thenReturn(savedUser);

        UserResponseDTO response = userService.saveUser(userDto, mobNo);

        assertNotNull(response);
        assertEquals(userDto.mobileNumber(), response.mobileNumber());
        assertEquals(userDto.email(), response.email());
        verify(userRepo, times(1)).save(any(Users.class));
    }

    @Test
    void testSaveUser_DuplicateUserException() {
        String mobNo = "1234567890";
        UserDTO userDto = new UserDTO("user_name", "test@example.com", mobNo,"password");
        Users existingUser = new Users(1L, "user_name2", "test@example.com", mobNo, "password-2", AccountStatusEnum.ACTIVE);

        when(userRepo.findByMobileNumberOrEmail(userDto.mobileNumber(), userDto.email())).thenReturn(existingUser);

        DuplicateUserException exception = assertThrows(DuplicateUserException.class, () -> userService.saveUser(userDto, mobNo));
        assertEquals("User already registered with same mobile number or email", exception.getMessage());

        verify(userRepo, never()).save(any(Users.class));
    }

    @Test
    void testSaveUser_HeaderDetailsMismatch_RuntimeException() {
        UserDTO userDto = new UserDTO("user_name", "test@example.com", "1234567890","password");

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.saveUser(userDto, "098"));
        assertEquals("Could not verify user details", exception.getMessage());

        verify(userRepo, never()).save(any(Users.class));
    }

    @Test
    void testGetUserDetails_Success() {
        // Given
        Long userId = 1L;
        Users user = new Users();
        user.setUserId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setMobileNumber("1234567890");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserDetails(userId);

        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals("John Doe", response.name());
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testGetUserDetails_UserNotFoundException() {
        Long userId = 2L;
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserDetails(userId));

        assertEquals("User with id 2 Not Found", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testValidateUser_Success() {
        // Given
        Long userId = 1L;
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Users user = new Users();
        user.setUserId(userId);
        user.setStatus(AccountStatusEnum.ACTIVE);
        user.setPassword(encodedPassword);

        ValidationRequestDto requestDto = new ValidationRequestDto(userId, rawPassword);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean isValid = userService.validateUser(requestDto);

        // Then
        assertTrue(isValid, "Expected user validation to be successful");
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testValidateUser_UserInactive() {
        // Given
        Long userId = 2L;
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Users user = new Users();
        user.setUserId(userId);
        user.setStatus(AccountStatusEnum.BLOCKED);
        user.setPassword(encodedPassword);

        ValidationRequestDto requestDto = new ValidationRequestDto(userId, rawPassword);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean isValid = userService.validateUser(requestDto);

        // Then
        assertFalse(isValid, "Expected validation to fail for inactive user");
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testValidateUser_WrongPassword() {
        // Given
        Long userId = 3L;
        String rawPassword = "password123";
        String wrongPassword = "wrongPassword!";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Users user = new Users();
        user.setUserId(userId);
        user.setStatus(AccountStatusEnum.ACTIVE);
        user.setPassword(encodedPassword);

        ValidationRequestDto requestDto = new ValidationRequestDto(userId, wrongPassword);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean isValid = userService.validateUser(requestDto);

        // Then
        assertFalse(isValid, "Expected validation to fail for wrong password");
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testValidateUser_UserNotFound() {
        // Given
        Long userId = 4L;
        ValidationRequestDto requestDto = new ValidationRequestDto(userId, "password123");

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean isValid = userService.validateUser(requestDto);

        // Then
        assertFalse(isValid, "Expected validation to fail for non-existing user");
        verify(userRepo, times(1)).findById(userId);
    }

}
