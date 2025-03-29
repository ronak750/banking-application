package com.transactions.gatewayconnector.service;

import com.transactions.gatewayconnector.dto.*;
import com.transactions.gatewayconnector.dto.request.NeftTransferRequestDto;
import com.transactions.gatewayconnector.dto.request.UPITransferRequestDTO;
import com.transactions.gatewayconnector.dto.request.WalletTransferRequestDto;
import com.transactions.gatewayconnector.dto.response.BalanceResponseDTO;
import com.transactions.gatewayconnector.dto.response.TransactionResponseDto;
import com.transactions.gatewayconnector.dto.response.ValidationResponseDTO;
import com.transactions.gatewayconnector.paymentgateway.Razorpay;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RazorpayAdapter implements IAdapter {

    private final Razorpay razorpay;

    public TransactionResponseDto transferWalletMoney(WalletTransferRequestDto walletTransferRequestDto) {
        var paymentGatewayResponse = razorpay.transferWalletMoney(
                walletTransferRequestDto.transactionId(),
                walletTransferRequestDto.fromWalletId(),
                walletTransferRequestDto.toWalletId(),
                walletTransferRequestDto.amount()
        );
        return buildTransactionResponseDTOFromRazorpayResponse(paymentGatewayResponse);
    }

    public TransactionResponseDto transferUPIMoney(UPITransferRequestDTO upiTransferRequestDTO) {
        var paymentGatewayResponse = razorpay.transferUPIMoney(
                upiTransferRequestDTO.transactionId(),
                upiTransferRequestDTO.fromUpiId(),
                upiTransferRequestDTO.toUpiId(),
                upiTransferRequestDTO.pin(),
                upiTransferRequestDTO.amount()
        );
        return buildTransactionResponseDTOFromRazorpayResponse(paymentGatewayResponse);
    }

    public List<TransactionResponseDto> transferNEFTMoney(List<NeftTransferRequestDto> neftTransferRequestDtoList) {

        return razorpay.transferNEFTMoney(neftTransferRequestDtoList).stream()
                .map(this::buildTransactionResponseDTOFromRazorpayResponse)
                .toList();
    }

    public ValidationResponseDTO validateUPI(String upiId) {
        return new ValidationResponseDTO(razorpay.validateUPI(upiId));
    }

    public BalanceResponseDTO checkUPIBalance(String upiId, int pin) {
        return new BalanceResponseDTO(razorpay.checkUPIBalance(upiId, pin));
    }

    public BalanceResponseDTO checkWalletBalance(String walletId) {
        return new BalanceResponseDTO(razorpay.checkWalletBalance(walletId));
    }

    private TransactionResponseDto buildTransactionResponseDTOFromRazorpayResponse(
            RazorpayTransactionResponseDTO razorpayTransactionResponseDTO
            ) {
        return new TransactionResponseDto(
                razorpayTransactionResponseDTO.getTransactionId(),
                razorpayTransactionResponseDTO.getAmount(),
                Boolean.TRUE.equals(razorpayTransactionResponseDTO.getIsSuccess()) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED,
                razorpayTransactionResponseDTO.getMessage(),
                razorpayTransactionResponseDTO.getTime()
        );
    }

}
