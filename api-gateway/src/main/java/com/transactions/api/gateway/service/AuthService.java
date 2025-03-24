package com.transactions.api.gateway.service;

import com.transactions.api.gateway.clients.UserManagementFeignClient;
import com.transactions.api.gateway.dtos.LoginDto;
import com.transactions.api.gateway.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserManagementService userManagementService;

    public AuthService(JwtUtil jwtUtil, UserManagementService userManagementService) {
        this.jwtUtil = jwtUtil;
        this.userManagementService = userManagementService;
    }

//    public ResponseEntity<?> login(LoginDto credentials) {
//        String username = credentials.userId().toString();
//        String password = credentials.password();
//
//        UserDTO userDetails;
//        try {
//            userDetails = userManagementFeignClient.getUser(credentials.userId());
//        } catch (Exception e) {
//            return ResponseEntity.status(503).body(Map.of("error", "Could not verify your details. Please try after some time"));
//        }
//
//        if (userDetails == null || !doesPasswordMatch(password, userDetails.password())) {
//            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
//        }
//
//        String token = jwtUtil.generateToken(username);
//        return ResponseEntity.ok(Map.of("accessToken", token));
//    }



//    public Mono<ResponseEntity<Map<String, String>>> login(LoginDto credentials) {
//        String username = credentials.userId().toString();
//        String password = credentials.password();
//
//        return userManagementFeignClient.getUser(credentials.userId())
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMap(userDetails -> {
//                    if (userDetails == null || !doesPasswordMatch(password, userDetails.password())) {
//                        return Mono.just(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
//                    }
//
//                    String token = jwtUtil.generateToken(username);
//                    return Mono.just(ResponseEntity.ok(Map.of("accessToken", token)));
//                })
//                .onErrorResume(throwable -> {
//                    return Mono.just(ResponseEntity.status(503).body(Map.of("error", "Could not verify your details. Please try after some time")));
//                });
//    }

    public Mono<ResponseEntity<Map<String, String>>> login(LoginDto credentials) {
        String username = credentials.userId().toString();

        return userManagementService.getUser(credentials)
                .flatMap(uservalidation -> {
                    if (uservalidation == null || !uservalidation) {
                        return Mono.defer(() -> Mono.just(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials or user is blocked"))));
                    }

                    String token = jwtUtil.generateToken(username);
                    return Mono.defer(() -> Mono.just(ResponseEntity.ok(Map.of("accessToken", token))));
                })
                .onErrorResume(throwable ->
                        Mono.defer(() -> Mono.just(ResponseEntity.status(503).body(Map.of("error", "Could not verify your details. Please try after some time"))))
                );
    }
}
