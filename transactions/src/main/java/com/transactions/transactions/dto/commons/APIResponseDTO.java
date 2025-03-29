package com.transactions.transactions.dto.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponseDTO {
    @NotNull private StatusModel statusModel;
    private String responseMsg;
    private Object response;

    public APIResponseDTO(StatusModel statusModel, String responseMsg, Object response) {
        this.statusModel = statusModel;
        this.responseMsg = responseMsg;
        this.response = response;
    }
}