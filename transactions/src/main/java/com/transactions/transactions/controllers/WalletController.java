package com.transactions.transactions.controllers;

import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.WalletTransactionResponseDTO;
import com.transactions.transactions.dtos.WalletTransferRequestDto;
import com.transactions.transactions.exceptions.TransactionRequestFailedException;
import com.transactions.transactions.services.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("api/v1/wallets")
@AllArgsConstructor
@Validated
public class WalletController {

    private WalletService walletService;

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@Min(value = 1) @PathVariable Long walletId) throws TransactionRequestFailedException {
        BigDecimal balance = walletService.getBalance(walletId).setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/transfer")
    public ResponseEntity<WalletTransactionResponseDTO> transferMoney(@Valid @RequestBody WalletTransferRequestDto transferRequest) throws Exception {
        var response = walletService.transferMoney(transferRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransactionResponseDTO>> getTransactions(@PathVariable Long walletId) {
        List<WalletTransactionResponseDTO> transactions = walletService.getTransactions(walletId);
        if(transactions.isEmpty()) {
            return new ResponseEntity<>(transactions, HttpStatus.NOT_FOUND);
        }
        else {
            return ResponseEntity.ok(transactions);
        }
    }

}