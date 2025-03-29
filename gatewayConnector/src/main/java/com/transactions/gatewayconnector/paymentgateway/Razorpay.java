package com.transactions.gatewayconnector.paymentgateway;

import com.transactions.gatewayconnector.dto.request.NeftTransferRequestDto;
import com.transactions.gatewayconnector.dto.RazorpayTransactionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static com.transactions.gatewayconnector.constant.Constants.TRANSACTION_SUCCESSFUL;

@Component
public class Razorpay {

    private static final Logger log = LoggerFactory.getLogger(Razorpay.class);

    private final Random r = new Random();

    private static final String TRANSACTION_FAILED_DUE_TO_INSUFFICIENT_BALANCE = "Transaction failed due to insufficient balance";
    private static final String DOES_NOT_EXIST_MSG = " does not exist";

    public RazorpayTransactionResponseDTO transferWalletMoney(String transactionId, Long fromWalletId, Long toWalletId, double amount) {

        // unnecessary logging to avoid sonar unused variables problem
        log.debug("transferWalletMoney called with transactionId: {}, fromWalletId: {}, toWalletId: {}, amount: {}", transactionId, fromWalletId, toWalletId, amount);

        if(amount <= 0) {
            throw new IllegalArgumentException("Transfer amount cannot be negative");
        }
        if(amount < 100) {
            return new RazorpayTransactionResponseDTO(
                    true,
                    TRANSACTION_SUCCESSFUL,
                    transactionId,
                    amount,
                    LocalDateTime.now()
            );
        }
        else if(amount < 1000) {
            return new RazorpayTransactionResponseDTO(
                    false,
                    TRANSACTION_FAILED_DUE_TO_INSUFFICIENT_BALANCE,
                    transactionId,
                    amount,
                    LocalDateTime.now()
            );
        }
        else {
            throw new IllegalArgumentException("Wallet with id " + toWalletId + DOES_NOT_EXIST_MSG);
        }
    }

    public RazorpayTransactionResponseDTO transferUPIMoney(String transactionId, String fromUpiId, String toUpiId, int pin, double amount) {
        // unnecessary logging to avoid sonar unused variables problem
        log.debug("transferUPIMoney called with transactionId: {}, fromUpiId: {}, toUpiId: {}, pin: {}, amount: {}", transactionId, fromUpiId, toUpiId, pin, amount);

        if(amount <= 0) {
            throw new IllegalArgumentException("Transfer amount cannot be negative");
        }
        if(amount < 100) {
            return new RazorpayTransactionResponseDTO(
                    true,
                    TRANSACTION_SUCCESSFUL,
                    transactionId,
                    amount,
                    LocalDateTime.now()
            );
        }
        else if(amount < 1000) {
            return new RazorpayTransactionResponseDTO(
                    false,
                    TRANSACTION_FAILED_DUE_TO_INSUFFICIENT_BALANCE,
                    transactionId,
                    amount,
                    LocalDateTime.now()
            );
        }
        else {
            throw new IllegalArgumentException("Upi id " + toUpiId + DOES_NOT_EXIST_MSG);
        }
    }

    public List<RazorpayTransactionResponseDTO> transferNEFTMoney(List<NeftTransferRequestDto> neftTransferRequestDtoList) {
        return neftTransferRequestDtoList.stream().map(neftTransferRequestDto -> {
             if(neftTransferRequestDto.amount() > 1000) {
                 return new RazorpayTransactionResponseDTO(
                         false,
                         TRANSACTION_FAILED_DUE_TO_INSUFFICIENT_BALANCE,
                         neftTransferRequestDto.transactionId(),
                         neftTransferRequestDto.amount(),
                         LocalDateTime.now()
                 );
             }
             else {
                 return new RazorpayTransactionResponseDTO(
                         true,
                         TRANSACTION_SUCCESSFUL,
                         neftTransferRequestDto.transactionId(),
                         neftTransferRequestDto.amount(),
                         LocalDateTime.now()
                 );
             }
        }).toList();
    }

    public boolean validateUPI(String upiId) {
        // unnecessary logging to avoid sonar unused variables problem
        log.debug("validateUPI called with upiId: {}", upiId);

        return getRandomBoolean();
    }

    public BigDecimal checkUPIBalance(String upiId, int pin) {
        // unnecessary logging to avoid sonar unused variables problem
        log.debug("checkUPIBalance called with upiId: {}, pin: {}", upiId, pin);

        if(getRandomBoolean())
            throw new IllegalArgumentException("Upi id " + upiId + DOES_NOT_EXIST_MSG);
        if(getRandomBoolean())
            throw new IllegalArgumentException("Incorrect pin provided");
        return BigDecimal.valueOf(Math.random() * 100);
    }

    public BigDecimal checkWalletBalance(String walletId) {
        if(getRandomBoolean())
            throw new IllegalArgumentException("Wallet with id " + walletId + DOES_NOT_EXIST_MSG);
        else
            return BigDecimal.valueOf(Math.random() * 100);
    }

    private boolean getRandomBoolean() {
        int rand = r.nextInt(2);  // returns pseudo-random value between 0 and 50
        return rand == 0;
    }

}
