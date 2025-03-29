package com.transactions.users.utils;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.entities.Users;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class UtilityClassTests {

    @Test
    public void testConvertUserDtoToUser_AllFieldsPopulated() {
        // Arrange
        UserDTO userDTO = new UserDTO(
                "John Doe",
                "john.doe@example.com",
                "1234567890",
                "password123"
        );
        Users existingUser = new Users();

        // Act
        Users resultUser = UtilityClass.convertUserDtoToUser(userDTO, existingUser);

        // Assert
        assertEquals(userDTO.name(), resultUser.getName());
        assertEquals(userDTO.email(), resultUser.getEmail());
        assertEquals(userDTO.mobileNumber(), resultUser.getMobileNumber());
        assertEquals(AccountStatusEnum.ACTIVE, resultUser.getStatus());

        // Verify password is encoded
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertTrue(passwordEncoder.matches(
                userDTO.password(),
                resultUser.getPassword()
        ));
    }

    @Test
    public void testConvertUserDtoToUser_PasswordEncoding() {
        // Arrange
        UserDTO userDTO = new UserDTO(
                "Jane Doe",
                "jane.doe@example.com",
                "0987654321",
                "securePassword456"
        );
        Users existingUser = new Users();

        // Act
        Users resultUser = UtilityClass.convertUserDtoToUser(userDTO, existingUser);

        // Assert
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Check that the stored password is different from the original
        assertNotEquals(userDTO.password(), resultUser.getPassword());

        // Verify that the encoded password can be matched
        assertTrue(passwordEncoder.matches(
                userDTO.password(),
                resultUser.getPassword()
        ));
    }

    @Test
    public void testConvertUserToUserResponseDto_FullConversion() {
        // Arrange
        Users user = new Users();
        user.setUserId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setMobileNumber("1234567890");
        user.setStatus(AccountStatusEnum.ACTIVE);

        // Act
        UserResponseDTO responseDTO = UtilityClass.convertUserToUserResponseDto(user);

        // Assert
        assertEquals(user.getUserId(), responseDTO.id());
        assertEquals(user.getName(), responseDTO.name());
        assertEquals(user.getEmail(), responseDTO.email());
        assertEquals(user.getMobileNumber(), responseDTO.mobileNumber());
        assertEquals(user.getStatus(), responseDTO.status());
    }


    @Test
    public void testConvertUserDtoToUser_NullInput() {
        // Arrange
        UserDTO userDTO = null;
        Users existingUser = new Users();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            UtilityClass.convertUserDtoToUser(userDTO, existingUser);
        });
    }
}
