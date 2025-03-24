package com.transactions.transactions.clinets;

import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.UPITransferRequestDTO;
import com.transactions.transactions.dtos.WalletTransferRequestDto;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

//@FeignClient(url = "http://localhost:9000/", name = "gatewayConnector")
//@FeignClient(name = "gatewayconnector")
@FeignClient(name = "gatewayconnector", fallbackFactory = GatewayConnectorFallbackFactory.class)
public interface GatewayConnectorFeignClient {

    @PostMapping(value = "api/v1/connector-gateway/transfer-wallet-money", consumes = "application/json")
    TransactionResponseDTO transferWalletMoney(@RequestBody
                                                      WalletTransferRequestDto walletTransferRequestDto);

    @PostMapping(value = "api/v1/connector-gateway/transfer-upi-money", consumes = "application/json")
    TransactionResponseDTO transferUpiMoney(@Valid
                                                   @RequestBody
                                            UPITransferRequestDTO upiTransferRequestDTO);

    @PostMapping(value = "api/v1/connector-gateway/transfer-neft-money", consumes = "application/json")
    List<TransactionResponseDTO> transferNEFTMoney(@RequestBody List<NEFTtransferRequestDto> neftTransferRequestDtoList);


    @PostMapping(value = "api/v1/connector-gateway/upi-balance", consumes = "application/json")
    BigDecimal checkUPIBalance(@Valid @RequestBody UPIBalanceCheckRequestDto upiAndPINRequestDto);

    @PostMapping(value = "api/v1/connector-gateway/upi-validation", consumes = "application/json")
    boolean validateUPI(@NotEmpty @RequestBody String upiId);

    @PostMapping(value = "api/v1/connector-gateway/wallet-balance", consumes = "application/json")
    BigDecimal checkWalletBalance(@NotEmpty @RequestBody String walletId);
}
