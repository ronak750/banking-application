package com.transactions.api.gateway.dto.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class StatusModel {
    private int statusCode;
    private String statusMsg;
}
