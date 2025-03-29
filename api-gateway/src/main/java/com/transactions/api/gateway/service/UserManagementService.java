package com.transactions.api.gateway.service;

import com.transactions.api.gateway.dto.LoginDto;
import com.transactions.api.gateway.dto.commons.APIResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

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
                .bodyToMono(APIResponseDTO.class)
                .map(apiResponse -> {
                    // Check if response is null or not of expected type
                    if (apiResponse.getResponse() == null) {
                        return false;
                    }

                    if (apiResponse.getResponse() instanceof Map) {
                        Map<String, Object> responseMap = (Map<String, Object>) apiResponse.getResponse();
                        return Boolean.TRUE.equals(responseMap.get("isValid"));
                    } else if (apiResponse.getResponse() instanceof Boolean) {
                        return (Boolean) apiResponse.getResponse();
                    } else {
                        return false;
                    }
                })
                .onErrorResume(ex ->
                    Mono.just(false)
                );
    }
}
