package com.transactions.users.controllers;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.dtos.ValidationRequestDto;
import com.transactions.users.dtos.commons.SuccessResponseDTO;
import com.transactions.users.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1")
@Validated
public class UsersController {

    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<SuccessResponseDTO<UserResponseDTO>> createUser(
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader(value = "X-Mobile-Number") String headerMobileNumber
    ) {
        return ResponseEntity.ok(
                new SuccessResponseDTO<>(
                        userService.saveUser(userDTO, headerMobileNumber),
                        "User Creation Successful"
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO<UserResponseDTO>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                new SuccessResponseDTO<>(
                    userService.getUserDetails(id),
                    "User Creation Successful"
                )
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<SuccessResponseDTO<Boolean>> validateUser(
            @Valid @RequestBody ValidationRequestDto validationRequestDto
    ) {
        return ResponseEntity.ok(
                new SuccessResponseDTO<>(
                        userService.validateUser(validationRequestDto),
                        "User Validation Status Fetched Successfully"
                )
        );
    }
}
