package com.transactions.users.controllers;

import com.transactions.users.dtos.UserDTO;
import com.transactions.users.dtos.UserResponseDTO;
import com.transactions.users.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.saveUser(userDTO), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable Long id) {
        return userService.getUserDetails(id);
    }

    @GetMapping("/{id}/active")
    public boolean getUserActiveStatus(@PathVariable Long id) {
        return userService.isActiveUser(id);
    }
}
