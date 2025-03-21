package com.transactions.gatewayConnector.controller;

import com.transactions.gatewayConnector.dto.NeftTransferRequestDto;
import com.transactions.gatewayConnector.dto.TransactionResponseDto;
import com.transactions.gatewayConnector.dto.UPITransferRequestDTO;
import com.transactions.gatewayConnector.dto.WalletTransferRequestDto;
import com.transactions.gatewayConnector.dto.request.UPIAndPINRequestDto;
import com.transactions.gatewayConnector.paymentGateway.Razorpay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/connector-gateway")
public class ConnectorGatewayController {

    private final Razorpay razorpay;

    @PostMapping("/transfer-wallet-money")
    public TransactionResponseDto transferWalletMoney(@Valid
                                                          @RequestBody
                                                          WalletTransferRequestDto walletTransferRequestDto) {
        return razorpay.transferWalletMoney(
                walletTransferRequestDto.transactionId(),
                walletTransferRequestDto.fromWalletId(),
                walletTransferRequestDto.toWalletId(),
                walletTransferRequestDto.amount()
        );
    }

    @PostMapping("/transfer-upi-money")
    public TransactionResponseDto transferUpiMoney(@Valid
                                                      @RequestBody
                                                       UPITransferRequestDTO upiTransferRequestDTO) {
        return razorpay.transferUPIMoney(
                upiTransferRequestDTO.transactionId(),
                upiTransferRequestDTO.fromUpiId(),
                upiTransferRequestDTO.toUpiId(),
                upiTransferRequestDTO.pin(),
                upiTransferRequestDTO.amount()
        );
    }

    @PostMapping("/transfer-neft-money")
    public List<TransactionResponseDto> transferNEFTMoney(@RequestBody List<NeftTransferRequestDto> neftTransferRequestDtoList) {
        return razorpay.transferNEFTMoney(neftTransferRequestDtoList);
    }

    @PostMapping("/upi-balance")
    public BigDecimal checkUPIBalance(@Valid @RequestBody UPIAndPINRequestDto upiAndPINRequestDto) {
        return razorpay.checkUPIBalance(upiAndPINRequestDto.upiId(), upiAndPINRequestDto.pin());
    }

    @PostMapping("/upi-validation")
    public boolean validateUPI(@NotEmpty @RequestBody String upiId) {
        return razorpay.validateUPI(upiId);
    }

    @PostMapping("/wallet-balance")
     public BigDecimal checkWalletBalance(@NotEmpty @RequestBody String walletId) {
        return razorpay.checkWalletBalance(walletId);
     }

}

