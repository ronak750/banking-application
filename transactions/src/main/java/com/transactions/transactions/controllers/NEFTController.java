package com.transactions.transactions.controllers;

import com.transactions.transactions.dtos.TransactionResponseDTO;
import com.transactions.transactions.dtos.request.NEFTtransferRequestDto;
import com.transactions.transactions.services.NEFTService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/neft")
@AllArgsConstructor
@Validated
public class NEFTController {

    private NEFTService neftService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transferMoney(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody NEFTtransferRequestDto nefTtransferRequestDto
    ) throws Exception {
        TransactionResponseDTO response = neftService.transferMoney(nefTtransferRequestDto, userId);
        return ResponseEntity.ok(response);
    }
}