package com.transactions.transactions.dto.response;

import com.transactions.transactions.dto.commons.StatusModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GatewayConnectorTransactionListResponseDTO {
    private StatusModel statusModel;
    private String responseMsg;
    private List<TransactionResponseDTO> response;
}
