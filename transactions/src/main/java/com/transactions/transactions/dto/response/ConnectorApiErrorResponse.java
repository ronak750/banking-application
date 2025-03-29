package com.transactions.transactions.dto.response;

import com.transactions.transactions.dto.commons.StatusModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectorApiErrorResponse {
    private StatusModel statusModel;
    private String responseMsg;
}
