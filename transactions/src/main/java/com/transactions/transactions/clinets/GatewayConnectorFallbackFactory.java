package com.transactions.transactions.clinets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dtos.ConnectorApiErrorResponse;
import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.UPITransferRequestDTO;
import com.transactions.transactions.dtos.WalletTransferRequestDto;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.exceptions.ApiException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.transactions.transactions.utils.Constants.TRANSACTION_REQUEST_FAILED;

@Component
public class GatewayConnectorFallbackFactory implements FallbackFactory<GatewayConnectorFeignClient> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GatewayConnectorFeignClient create(Throwable cause) {
        return new GatewayConnectorFeignClient() {
            @Override
            public TransactionResponseDTO transferWalletMoney(WalletTransferRequestDto walletTransferRequestDto) {
                throw handleFallbacks(cause);
            }

            @Override
            public TransactionResponseDTO transferUpiMoney(UPITransferRequestDTO upiTransferRequestDTO) {
                throw handleFallbacks(cause);
            }

            @Override
            public List<TransactionResponseDTO> transferNEFTMoney(List<NEFTtransferRequestDto> neftTransferRequestDtoList) {
                throw handleFallbacks(cause);
            }

            @Override
            public BigDecimal checkUPIBalance(UPIBalanceCheckRequestDto upiAndPINRequestDto) {
                throw handleFallbacks(cause);
            }

            @Override
            public boolean validateUPI(String upiId) {
                throw handleFallbacks(cause);
            }

            @Override
            public BigDecimal checkWalletBalance(String walletId) {
                throw new ApiException(1,"","");
            }
        };
    }

    private ApiException handleFallbacks(Throwable cause) {
        if (cause instanceof FeignException feignException) {
            if (feignException.status() == 400) {
                String errorBody = feignException.contentUTF8();

                String errorMessage = "";

                try {
                    ConnectorApiErrorResponse errorResponse = objectMapper.readValue(errorBody, ConnectorApiErrorResponse.class);
                    errorMessage = errorResponse.getMessage();
                } catch (Exception e) {
                    errorMessage = feignException.getMessage();
                }

                return new ApiException(
                        feignException.status(),
                        errorMessage,
                        TRANSACTION_REQUEST_FAILED
                );
            }
            else {
                return new ApiException(feignException.status(), cause.getMessage(), "UNKNOWN");
            }
        }

        return new ApiException(500, "Error processing response", "UNKNOWN");
    }
}