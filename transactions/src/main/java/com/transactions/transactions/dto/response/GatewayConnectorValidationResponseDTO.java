package com.transactions.transactions.dto.response;

import com.transactions.transactions.dto.commons.StatusModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GatewayConnectorValidationResponseDTO {
    private StatusModel statusModel;
    private String responseMsg;
    private ValidationResponseDTO response;
}
