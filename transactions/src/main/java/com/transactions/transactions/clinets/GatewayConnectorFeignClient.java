package com.transactions.transactions.clinets;

import com.transactions.transactions.dto.response.GatewayConnectorBalanceResponseDTO;
import com.transactions.transactions.dto.response.GatewayConnectorTransactionListResponseDTO;
import com.transactions.transactions.dto.response.GatewayConnectorTransactionResponseDTO;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.WalletTransferRequestDto;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.dto.response.GatewayConnectorValidationResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "gatewayconnector", fallbackFactory = GatewayConnectorFallbackFactory.class)
public interface GatewayConnectorFeignClient {

    @PostMapping(value = "api/v1/connector-gateway/transfer-wallet-money", consumes = "application/json")
    GatewayConnectorTransactionResponseDTO transferWalletMoney(@RequestBody
                                                      WalletTransferRequestDto walletTransferRequestDto);

    @PostMapping(value = "api/v1/connector-gateway/transfer-upi-money", consumes = "application/json")
    GatewayConnectorTransactionResponseDTO transferUpiMoney(@Valid
                                                   @RequestBody
                                            UPITransferRequestDTO upiTransferRequestDTO);

    @PostMapping(value = "api/v1/connector-gateway/transfer-neft-money", consumes = "application/json")
    GatewayConnectorTransactionListResponseDTO transferNEFTMoney(@RequestBody List<NEFTtransferRequestDto> neftTransferRequestDtoList);


    @PostMapping(value = "api/v1/connector-gateway/upi-balance", consumes = "application/json")
    GatewayConnectorBalanceResponseDTO checkUPIBalance(@Valid @RequestBody UPIBalanceCheckRequestDto upiAndPINRequestDto);

    @PostMapping(value = "api/v1/connector-gateway/upi-validation", consumes = "application/json")
    GatewayConnectorValidationResponseDTO validateUPI(@NotEmpty @RequestBody String upiId);

    @PostMapping(value = "api/v1/connector-gateway/wallet-balance", consumes = "application/json")
    GatewayConnectorBalanceResponseDTO checkWalletBalance(@NotEmpty @RequestBody String walletId);
}
