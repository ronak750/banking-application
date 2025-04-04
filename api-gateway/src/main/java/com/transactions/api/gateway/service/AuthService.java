package com.transactions.api.gateway.service;

import com.transactions.api.gateway.dto.APITokenResponseDTO;
import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import com.transactions.api.gateway.dto.commons.StatusModel;
import com.transactions.api.gateway.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.transactions.api.gateway.constant.Constants.INVALID_CREDENTIALS;
import static com.transactions.api.gateway.constant.Constants.SERVICE_UNAVAILABLE;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserManagementService userManagementService;

    public AuthService(JwtUtil jwtUtil, UserManagementService userManagementService) {
        this.jwtUtil = jwtUtil;
        this.userManagementService = userManagementService;
    }

    public Mono<ResponseEntity<APIResponseDTO>> login(LoginDto credentials) {
        String username = credentials.userId().toString();

        return userManagementService.getUser(credentials)
                .flatMap(userValidation -> {
                    // Improved null and boolean validation
                    if (Boolean.FALSE.equals(userValidation)) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(APIResponseDTO.builder()
                                        .responseMsg("Invalid credentials or user is blocked")
                                        .statusModel(new StatusModel(401, INVALID_CREDENTIALS))
                                        .build()
                                )
                        );
                    }

                    // Generate token and create successful response
                    String token = jwtUtil.generateToken(username);
                    return Mono.just(ResponseEntity.ok(
                            APIResponseDTO.builder()
                                    .responseMsg("Login successful")
                                    .response(new APITokenResponseDTO(token))
                                    .statusModel(new StatusModel(200, "SUCCESS"))
                                    .build()
                    ));
                })
                .onErrorResume(throwable -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(APIResponseDTO.builder()
                                    .responseMsg("Could not verify your details. Please try after some time")
                                    .statusModel(new StatusModel(503, SERVICE_UNAVAILABLE))
                                    .build()
                            ));
                });
    }
}
