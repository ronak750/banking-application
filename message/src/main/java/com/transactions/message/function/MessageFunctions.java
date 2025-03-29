package com.transactions.message.function;

import com.transactions.message.clients.UsersServiceFeignClient;
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
            try{
                var userServiceResponseDTO = usersServiceFeignClient.getUser(Long.valueOf(messageInfoDto.userId()));
                UserResponseDTO user = userServiceResponseDTO.getResponse();
                log.info("Sending email at {} with the details : {}", user.email(), messageInfoDto.message());
            } catch (Exception e){
                log.info("Failed to send email to user, with id {} ", messageInfoDto.userId());
            }

            return messageInfoDto;
        };
    }

    @Bean
    public Function<MessageInfoDto,MessageInfoDto> sms() {
        return messageInfoDto -> {
            try{
                var userServiceResponseDTO = usersServiceFeignClient.getUser(Long.valueOf(messageInfoDto.userId()));
                UserResponseDTO user = userServiceResponseDTO.getResponse();
                log.info("Sending sms at {} with the details : {}", user.mobileNumber(), messageInfoDto.message());
            } catch (Exception e){
                log.info("Failed to send sms to user, with id {} ", messageInfoDto.userId());
            }

            return messageInfoDto;
        };
    }

}
