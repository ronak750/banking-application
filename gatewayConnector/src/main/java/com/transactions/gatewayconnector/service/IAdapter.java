package com.transactions.gatewayconnector.service;


import com.transactions.gatewayconnector.dto.request.NeftTransferRequestDto;
import com.transactions.gatewayconnector.dto.response.TransactionResponseDto;
import com.transactions.gatewayconnector.dto.request.UPITransferRequestDTO;
import com.transactions.gatewayconnector.dto.request.WalletTransferRequestDto;
import com.transactions.gatewayconnector.dto.response.BalanceResponseDTO;
import com.transactions.gatewayconnector.dto.response.ValidationResponseDTO;

import java.util.List;

public interface IAdapter {

    TransactionResponseDto transferWalletMoney(WalletTransferRequestDto walletTransferRequestDto);

    TransactionResponseDto transferUPIMoney(UPITransferRequestDTO upiTransferRequestDTO);

    List<TransactionResponseDto> transferNEFTMoney(List<NeftTransferRequestDto> neftTransferRequestDtoList);

    ValidationResponseDTO validateUPI(String upiId);

    BalanceResponseDTO checkUPIBalance(String upiId, int pin);

    BalanceResponseDTO checkWalletBalance(String walletId);
}
