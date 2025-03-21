package com.transactions.users.utils;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.entities.Users;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UtilityClass {

    public static Users convertUserDtoToUser(UserDTO userDTO, Users user) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        user.setName(userDTO.name());
        user.setEmail(userDTO.email());
        user.setMobileNumber(userDTO.mobileNumber());
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setStatus(AccountStatusEnum.ACTIVE);
        return user;
    }

    public static UserResponseDTO convertUserToUserResponseDto(Users user) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getStatus()
        );
    }
}
