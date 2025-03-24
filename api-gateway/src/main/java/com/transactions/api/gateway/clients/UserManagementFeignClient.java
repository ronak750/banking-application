package com.transactions.api.gateway.clients;

import com.transactions.api.gateway.dtos.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@FeignClient(name = "users")
public interface UserManagementFeignClient {

    @GetMapping("/api/v1/{id}")
    Mono<UserDTO> getUser(@PathVariable Long id);
}
