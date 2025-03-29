package com.transactions.users.dtos.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ErrorDetails {
    private String code;
    private String details;
}
