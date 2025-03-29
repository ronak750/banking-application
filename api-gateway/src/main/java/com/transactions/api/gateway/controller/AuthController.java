package com.transactions.api.gateway.controller;

import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<APIResponseDTO>> login(@RequestBody LoginDto credentials) {
        return authService.login(credentials);
    }

}