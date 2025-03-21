package com.transactions.transactions.controllers;

import com.transactions.transactions.dtos.*;
import com.transactions.transactions.dtos.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.exceptions.TransactionRequestFailedException;
import com.transactions.transactions.services.UPIService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/upi")
public class UPIController {

    private final UPIService upiService;

    @PostMapping("/validate")
    public boolean validate(@Valid @RequestBody UPIValidationRequestDTO upiValidationRequestDTO) {
        return upiService.validateUPIid(upiValidationRequestDTO.upiId());
    }

    @PostMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@Valid @RequestBody UPIBalanceCheckRequestDto upiBalanceCheckRequestDto) throws TransactionRequestFailedException {
        BigDecimal balance = upiService.getBalance(upiBalanceCheckRequestDto).setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(@Valid
                                           @RequestBody UPITransferRequestDTO upiTransferRequestDTO) throws TransactionRequestFailedException {

        var response = upiService.transfer(upiTransferRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{upiId}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(@PathVariable String upiId) {
        List<TransactionResponseDTO> transactions = upiService.getTransactions(upiId);
        if(transactions.isEmpty()) {
            return new ResponseEntity<>(transactions, HttpStatus.NOT_FOUND);
        }
        else {
            return ResponseEntity.ok(transactions);
        }
    }
}
