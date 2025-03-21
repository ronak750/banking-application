package com.transactions.users.services;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.entities.Users;
import com.transactions.users.exceptions.DuplicateUserException;
import com.transactions.users.exceptions.UserNotFoundException;
import com.transactions.users.repos.UserRepo;
import com.transactions.users.utils.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    public UserResponseDTO saveUser(UserDTO userDto) {
        Users existingUser = userRepo.findByMobileNumberOrEmail(userDto.mobileNumber(), userDto.email());
        if(existingUser != null) {
            throw new DuplicateUserException("User already registered with same mobile number or email");
        }
        Users user = UtilityClass.convertUserDtoToUser(userDto, new Users());
        Users savedUser = userRepo.save(user);
        return UtilityClass
                .convertUserToUserResponseDto(savedUser);
    }

    public UserResponseDTO getUserDetails(Long id) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d Not Found", id)));
        return UtilityClass.convertUserToUserResponseDto(user);
    }

    public boolean isActiveUser(Long id) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d Not Found", id)));
        return user.getStatus().equals(AccountStatusEnum.ACTIVE);
    }
}
