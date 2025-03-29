package com.transactions.transactions.clinets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dto.response.*;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.WalletTransferRequestDto;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.exception.ApiException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.transactions.transactions.constant.Constants.TRANSACTION_REQUEST_FAILED;

@Component
public class GatewayConnectorFallbackFactory implements FallbackFactory<GatewayConnectorFeignClient> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(GatewayConnectorFallbackFactory.class);

    @Override
    public GatewayConnectorFeignClient create(Throwable cause) {
        return new GatewayConnectorFeignClient() {
            @Override
            public GatewayConnectorTransactionResponseDTO transferWalletMoney(WalletTransferRequestDto walletTransferRequestDto) {
                throw handleFallbacks(cause);
            }

            @Override
            public GatewayConnectorTransactionResponseDTO transferUpiMoney(UPITransferRequestDTO upiTransferRequestDTO) {
                throw handleFallbacks(cause);
            }

            @Override
            public GatewayConnectorTransactionListResponseDTO transferNEFTMoney(List<NEFTtransferRequestDto> neftTransferRequestDtoList) {
                throw handleFallbacks(cause);
            }

            @Override
            public GatewayConnectorBalanceResponseDTO checkUPIBalance(UPIBalanceCheckRequestDto upiAndPINRequestDto) {
                throw handleFallbacks(cause);
            }

            @Override
            public GatewayConnectorValidationResponseDTO validateUPI(String upiId) {
                throw handleFallbacks(cause);
            }

            @Override
            public GatewayConnectorBalanceResponseDTO checkWalletBalance(String walletId) {
                throw handleFallbacks(cause);
            }
        };
    }

    private ApiException handleFallbacks(Throwable cause) {
        logger.info(cause.getMessage());
        if (cause instanceof FeignException feignException) {
            if (feignException.status() == 400) {
                String errorBody = feignException.contentUTF8();

                String errorMessage = "";

                try {
                    ConnectorApiErrorResponse errorResponse = objectMapper.readValue(errorBody, ConnectorApiErrorResponse.class);
                    errorMessage = errorResponse.getResponseMsg();
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