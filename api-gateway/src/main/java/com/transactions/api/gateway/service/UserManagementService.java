package com.transactions.api.gateway.service;

import com.transactions.api.gateway.dtos.LoginDto;
import com.transactions.api.gateway.dtos.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserManagementService {

    private final WebClient webClient;

    public UserManagementService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("lb://USERS").build();
    }

    public Mono<Boolean> getUser(LoginDto loginDto) {
        return webClient.post()
                .uri("api/v1/validate")
                .bodyValue(loginDto)
                .retrieve()
                .bodyToMono(Boolean.class);
//                .onErrorResume(e -> Mono.empty()); // Handle errors gracefully
    }
}
