package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.dto.request.NEFTtransferRequestDto;
import com.transactions.transactions.services.NEFTService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    public ResponseEntity<APIResponseDTO> transferMoney(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody NEFTtransferRequestDto nefTtransferRequestDto
    ) {
        TransactionResponseDTO response = neftService.transferMoney(nefTtransferRequestDto, userId);
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .responseMsg("Request submitted successfully")
                        .response(response)
                        .statusModel(new StatusModel(202, "PENDING"))
                        .build()
        );
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<APIResponseDTO> getTransactionDetailsByTransactionId(
            @NotEmpty @PathVariable String transactionId
    ) {
        TransactionResponseDTO response = neftService.getTransactionDetailsByTransactionId(transactionId);
        return prepareGetTransactionDetailResponse(response);
    }

    private ResponseEntity<APIResponseDTO> prepareGetTransactionDetailResponse(TransactionResponseDTO response) {
        if(response.getStatus().equals(TransactionStatus.SUCCESS))
            return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(response)
                        .responseMsg("Transaction completed successfully")
                        .statusModel(new StatusModel(200, "SUCCESS"))
                        .build()
        );
        else if(response.getStatus().equals(TransactionStatus.FAILED))
            return ResponseEntity.badRequest().body(
                    APIResponseDTO.builder()
                            .response(response)
                            .statusModel(new StatusModel(400, "FAILED"))
                            .responseMsg("Transaction failed")
                            .build()
            );
        else
            return ResponseEntity.ok(
                    APIResponseDTO.builder()
                            .response(response)
                            .statusModel(new StatusModel(200, "SUCCESS"))
                            .responseMsg("Transaction Pending")
                            .build()
            );
    }
}