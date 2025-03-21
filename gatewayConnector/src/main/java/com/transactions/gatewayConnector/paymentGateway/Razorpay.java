package com.transactions.gatewayConnector.paymentGateway;

import com.transactions.gatewayConnector.dto.NeftTransferRequestDto;
import com.transactions.gatewayConnector.dto.TransactionResponseDto;
import com.transactions.gatewayConnector.dto.TransactionStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class Razorpay {
    public TransactionResponseDto transferWalletMoney(String transactionId, Long fromWalletId, Long toWalletId, double amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("Transfer amount cannot be negative");
        }
        if(amount < 100) {
            return new TransactionResponseDto(
                        transactionId,
                        amount,
                        TransactionStatus.SUCCESS,
                        "Transaction successful",
                        LocalDateTime.now()
            );
        }
        else if(amount < 1000) {
            return new TransactionResponseDto(
                    transactionId,
                    amount,
                    TransactionStatus.FAILED,
                   "Transaction failed due to insufficient balance",
                    LocalDateTime.now()
            );
        }
        else {
            throw new IllegalArgumentException("Wallet with id " + toWalletId + " does not exist");
        }
    }

    public TransactionResponseDto transferUPIMoney(String transactionId, String fromUpiId, String toUpiId, int pin, double amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("Transfer amount cannot be negative");
        }
        if(amount < 100) {
            return new TransactionResponseDto(
                    transactionId,
                    amount,
                    TransactionStatus.SUCCESS,
                    "Transaction successful",
                    LocalDateTime.now()
            );
        }
        else if(amount < 1000) {
            return new TransactionResponseDto(
                    transactionId,
                    amount,
                    TransactionStatus.FAILED,
                    "Transaction failed due to insufficient balance",
                    LocalDateTime.now()
            );
        }
        else {
            throw new IllegalArgumentException("Upi id " + toUpiId + " does not exist");
        }
    }

    public List<TransactionResponseDto> transferNEFTMoney(List<NeftTransferRequestDto> neftTransferRequestDtoList) {
        return neftTransferRequestDtoList.stream().map(neftTransferRequestDto -> {
             if(neftTransferRequestDto.amount() > 1000) {
                 return new TransactionResponseDto(
                         neftTransferRequestDto.transactionId(),
                         neftTransferRequestDto.amount(),
                         TransactionStatus.FAILED,
                         "Insufficient balance",
                         LocalDateTime.now()
                 );
             }
             else {
                 return new TransactionResponseDto(
                         neftTransferRequestDto.transactionId(),
                         neftTransferRequestDto.amount(),
                         TransactionStatus.SUCCESS,
                         "Transaction successful",
                         LocalDateTime.now()
                 );
             }
        }).toList();
    }

    public boolean validateUPI(String upiId) {
        return getRandomBoolean();
    }

    public BigDecimal checkUPIBalance(String upiId, int pin) {
        if(getRandomBoolean())
            throw new IllegalArgumentException("Upi id " + upiId + " does not exist");
        if(getRandomBoolean())
            throw new IllegalArgumentException("Incorrect pin provided");
        return BigDecimal.valueOf(Math.random() * 100);
    }

    public BigDecimal checkWalletBalance(String walletId) {
        if(getRandomBoolean())
            throw new IllegalArgumentException("Wallet with id " + walletId + " does not exist");
        else
            return BigDecimal.valueOf(Math.random() * 100);
    }

    private boolean getRandomBoolean() {
        return (int) (Math.random() * 10) % 2 == 0;
    }

}
