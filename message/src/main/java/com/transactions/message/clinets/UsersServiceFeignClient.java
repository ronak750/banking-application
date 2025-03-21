package com.transactions.message.clinets;

import com.transactions.message.dto.UserResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(url = "http://localhost:8080/", name = "users")
public interface UsersServiceFeignClient {

    @GetMapping("/{id}")
    UserResponseDTO getUser(@PathVariable Long id);
}
