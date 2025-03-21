package com.transactions.transactions.clinets;

import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.UPITransferRequestDTO;
import com.transactions.transactions.dtos.WalletTransferRequestDto;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FallbackFeign implements GatewayConnectorFeignClient{

    @Override
    public TransactionResponseDTO transferWalletMoney(WalletTransferRequestDto walletTransferRequestDto) {
        throw new RuntimeException("Service Unavailable");
    }

    @Override
    public TransactionResponseDTO transferUpiMoney(UPITransferRequestDTO upiTransferRequestDTO) {
        throw new RuntimeException("Service Unavailable");
    }

    @Override
    public List<TransactionResponseDTO> transferNEFTMoney(List<NEFTtransferRequestDto> neftTransferRequestDtoList) {
        throw new RuntimeException("Service Unavailable");
    }

    @Override
    public BigDecimal checkUPIBalance(UPIBalanceCheckRequestDto upiAndPINRequestDto) {
        throw new RuntimeException("Service Unavailable");
    }

    @Override
    public boolean validateUPI(String upiId) {
        throw new RuntimeException("Service Unavailable");
    }

    @Override
    public BigDecimal checkWalletBalance(String walletId) {
        throw new RuntimeException("Service Unavailable");
    }
}
