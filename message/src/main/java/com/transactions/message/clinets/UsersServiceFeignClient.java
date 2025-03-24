package com.transactions.message.clinets;

import com.transactions.message.dto.UserResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "users")
public interface UsersServiceFeignClient {

    @GetMapping("/{id}")
    UserResponseDTO getUser(@PathVariable Long id);
}
