package com.transactions.gatewayConnector.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@AllArgsConstructor
@Setter
@Getter
public class ErrorResponseDto implements Serializable {
    String errorCode;
    String message;
    HttpStatus statusCode;
}
