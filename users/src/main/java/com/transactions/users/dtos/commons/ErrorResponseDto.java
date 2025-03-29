package com.transactions.users.dtos.commons;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ErrorResponseDto extends BaseResponseDTO {
    ErrorDetails error;

    public ErrorResponseDto(String message, ErrorDetails error) {
        super(ResponseStatus.error, message);
        this.error = error;
    }
}
