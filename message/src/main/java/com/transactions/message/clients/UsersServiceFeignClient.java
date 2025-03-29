package com.transactions.message.clients;

import com.transactions.message.dto.UserServiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "users")
public interface UsersServiceFeignClient {

    @GetMapping("api/v1/{id}")
    UserServiceResponseDTO getUser(@PathVariable Long id);
}
