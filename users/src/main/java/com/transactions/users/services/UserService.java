package com.transactions.users.services;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.dtos.ValidationRequestDto;
import com.transactions.users.entities.AccountStatusEnum;
import com.transactions.users.entities.Users;
import com.transactions.users.exceptions.DuplicateUserException;
import com.transactions.users.exceptions.UserNotFoundException;
import com.transactions.users.repos.UserRepo;
import com.transactions.users.utils.UtilityClass;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service Layer for User Entity
 */
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /**
     * Save User and return UserResponseDTO
     * @param userDto
     * @return UserResponseDTO
     * @throws DuplicateUserException if user already registered with same mobile number or email
     */
    public UserResponseDTO saveUser(UserDTO userDto) {
        log.info("Attempting to save user with mobile number {} and email {}", userDto.mobileNumber(), userDto.email());
        Users existingUser = userRepo.findByMobileNumberOrEmail(userDto.mobileNumber(), userDto.email());
        if(existingUser != null) {
            log.info("User registration failed to due to user details already registered user with mobile number {} and email {}", userDto.mobileNumber(), userDto.email());
            throw new DuplicateUserException("User already registered with same mobile number or email");
        }
        Users user = UtilityClass.convertUserDtoToUser(userDto, new Users());
        Users savedUser = userRepo.save(user);
        log.info("Saved user with user id {}", savedUser.getUserId());
        return UtilityClass
                .convertUserToUserResponseDto(savedUser);
    }

    /**
     * Get user details by id
     * @param id
     * @return UserResponseDTO
     * @throws UserNotFoundException if user not found
     */
    public UserResponseDTO getUserDetails(Long id) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d Not Found", id)));
        return UtilityClass.convertUserToUserResponseDto(user);
    }

    /**
     * Check if user is active
     * @param id
     * @return true if user is active
     * @throws UserNotFoundException if user not found
     */
    public boolean isActiveUser(Long id) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d Not Found", id)));
        return user.getStatus().equals(AccountStatusEnum.ACTIVE);
    }

    /**
     * Validate user
     * @param validationRequestDto
     * @return true if user is valid
     */
    public boolean validateUser(ValidationRequestDto validationRequestDto) {
        Optional<Users> user = userRepo.findById(validationRequestDto.userId());
        if(user.isEmpty())
                return false;
        else {
            return  user.get().getStatus().equals(AccountStatusEnum.ACTIVE) &&
                    doesPasswordMatch(validationRequestDto.password(), user.get().getPassword());
        }
    }

    /**
     * Check if raw password matches with encoded password
     * @param rawPassword
     * @param encodedPassword
     * @return true if password matches
     */
    public boolean doesPasswordMatch(String rawPassword, String encodedPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}