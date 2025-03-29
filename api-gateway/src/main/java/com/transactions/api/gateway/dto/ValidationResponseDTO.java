package com.transactions.api.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResponseDTO {
    private Boolean isValid;

    public ValidationResponseDTO(Boolean isValid) {
        this.isValid = isValid;
    }
}