package com.transactions.users.dtos.commons;

import lombok.*;
import org.springframework.http.HttpStatus;


@Getter
@Setter
public class SuccessResponseDTO<T> extends BaseResponseDTO {
    private T data;

    public SuccessResponseDTO(T data, String message) {
        super(ResponseStatus.success, message);
        this.data = data;
    }
}
