package com.transactions.message.function;

import com.transactions.message.clinets.UsersServiceFeignClient;
import com.transactions.message.dto.MessageInfoDto;
import com.transactions.message.dto.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class MessageFunctions {
    private static final Logger log = LoggerFactory.getLogger(MessageFunctions.class);

    final UsersServiceFeignClient usersServiceFeignClient;

    public MessageFunctions(UsersServiceFeignClient usersServiceFeignClient) {
        this.usersServiceFeignClient = usersServiceFeignClient;
    }

    @Bean
    public Function<MessageInfoDto,MessageInfoDto> email() {
        return messageInfoDto -> {
            UserResponseDTO user = usersServiceFeignClient.getUser(Long.valueOf(messageInfoDto.userId()));
            log.info("Sending email at {} with the details : {}", user.email(), messageInfoDto.message());
            return messageInfoDto;
        };
    }

    @Bean
    public Function<MessageInfoDto,MessageInfoDto> sms() {
        return messageInfoDto -> {
            UserResponseDTO user = usersServiceFeignClient.getUser(Long.valueOf(messageInfoDto.userId()));
            log.info("Sending sms at {} with the details : {}", user.mobileNumber(), messageInfoDto.message());
            return messageInfoDto;
        };
    }

}
