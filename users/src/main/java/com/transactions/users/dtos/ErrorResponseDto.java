package com.transactions.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Setter
@Getter
public class ErrorResponseDto implements Serializable {
    String errorCode;
    String message;
}
