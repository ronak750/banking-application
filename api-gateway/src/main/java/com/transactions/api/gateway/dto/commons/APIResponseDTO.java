package com.transactions.api.gateway.dto.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponseDTO {
    @NotNull private StatusModel statusModel;
    private String responseMsg;
    private Object response;
}
