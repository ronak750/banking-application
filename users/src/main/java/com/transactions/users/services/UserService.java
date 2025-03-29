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
import jakarta.validation.ValidationException;
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
     * @param userDto Input User details, containg details of user
     * @param headerPhone Mobile number received from header, used for body data verification
     * @return UserResponseDTO containing user details in case of successful registeration
     * @throws DuplicateUserException if user already registered with same mobile number or email
     */
    public UserResponseDTO saveUser(UserDTO userDto, String headerPhone) {
        log.info("Attempting to save user with mobile number {} and email {}", userDto.mobileNumber(), userDto.email());

        validateUserRequestDto(userDto, headerPhone);

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
     * Validate user
     * @param validationRequestDto User id and password contained in request body
     * @return true if user is valid
     */
    public boolean validateUser(ValidationRequestDto validationRequestDto) {
        Optional<Users> user = userRepo.findById(validationRequestDto.userId());
        return user.filter(users -> users.getStatus().equals(AccountStatusEnum.ACTIVE) &&
                doesPasswordMatch(validationRequestDto.password(), users.getPassword())).isPresent();
    }

    private boolean doesPasswordMatch(String rawPassword, String encodedPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }


    private void validateUserRequestDto(UserDTO userDTO, String headerMobileNumber) {
        if(!headerMobileNumber.equals(userDTO.mobileNumber())){
            log.info("User registration failed to due to user details mismatch in body and header");
            throw new ValidationException("Could not verify user details");
        }

        checkUserDuplicate(userDTO);
    }

    private void checkUserDuplicate(UserDTO userDTO) {
        Users existingUser = userRepo.findByMobileNumberOrEmail(userDTO.mobileNumber(), userDTO.email());
        if(existingUser != null) {
            log.info("User registration failed to due to user details already registered user with mobile number {} and email {}", userDTO.mobileNumber(), userDTO.email());
            throw new DuplicateUserException("User already registered with same mobile number or email");
        }
    }
}