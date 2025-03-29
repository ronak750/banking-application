package com.transactions.transactions.dto.response;

import com.transactions.transactions.dto.commons.StatusModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GatewayConnectorTransactionResponseDTO {
    private StatusModel statusModel;
    private String responseMsg;
    private TransactionResponseDTO response;
}
