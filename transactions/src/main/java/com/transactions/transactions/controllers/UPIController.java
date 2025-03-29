package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.request.UPIBalanceCheckRequestDto;
import com.transactions.transactions.dto.request.UPITransferRequestDTO;
import com.transactions.transactions.dto.request.UPIValidationRequestDTO;
import com.transactions.transactions.dto.response.BalanceResponseDTO;
import com.transactions.transactions.dto.response.TransactionResponseDTO;
import com.transactions.transactions.services.UPIService;
import com.transactions.transactions.utils.UtilClass;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.transactions.transactions.constant.Constants.*;


@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/upi")
public class UPIController {

    private final UPIService upiService;

    @PostMapping("/validate")
    public ResponseEntity<APIResponseDTO> validate(@Valid @RequestBody UPIValidationRequestDTO upiValidationRequestDTO) {
        var validationResponse = upiService.validateUPIid(upiValidationRequestDTO.upiId());
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .statusModel(new StatusModel(200, SUCCESS))
                        .response(validationResponse)
                        .responseMsg(Boolean.TRUE.equals(validationResponse.getIsValid()) ? "UPI ID is valid" : "UPI ID is invalid")
                        .build()
        );
    }

    @PostMapping("/balance")
    public ResponseEntity<APIResponseDTO> getBalance(@Valid @RequestBody UPIBalanceCheckRequestDto upiBalanceCheckRequestDto) {
        BigDecimal balance = upiService.getBalance(upiBalanceCheckRequestDto).setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(new BalanceResponseDTO(balance))
                        .responseMsg("UPI balance successfully fetched")
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<APIResponseDTO> transfer(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UPITransferRequestDTO upiTransferRequestDTO
    ) {
        var response = upiService.transfer(upiTransferRequestDTO, userId);
        return prepareTransactionResponse(response);
    }

    @PostMapping("/transactions")
    public ResponseEntity<APIResponseDTO> getTransactions(
            @RequestBody UPIValidationRequestDTO upiValidationRequestDTO,
            @RequestParam (required = false, defaultValue = "10") String count,
            @RequestParam (required = false, defaultValue = "1") String page
    ) {
        Pageable pageable = UtilClass.getPageable(page, count);
        List<TransactionResponseDTO> transactions = upiService.getTransactions(upiValidationRequestDTO.upiId(), pageable);
        return prepareTransactionsGetData(transactions);
    }

    private ResponseEntity<APIResponseDTO> prepareTransactionsGetData(List<TransactionResponseDTO> transactions) {
        if(!transactions.isEmpty())
            return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(transactions)
                        .responseMsg("Transactions successfully fetched")
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
        else
            return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .responseMsg("No transactions found")
                        .statusModel(new StatusModel(404, RESOURCE_NOT_FOUND))
                        .build(),
                    HttpStatus.NOT_FOUND
        );
    }

    private ResponseEntity<APIResponseDTO> prepareTransactionResponse(TransactionResponseDTO response) {
        if(response.getStatus().equals(TransactionStatus.SUCCESS))
            return prepareSuccessResponse(response);
        else
            return prepareFailureResponse(response);
    }


    private ResponseEntity<APIResponseDTO> prepareFailureResponse(TransactionResponseDTO responseDTO) {
        return ResponseEntity.badRequest().body(
                APIResponseDTO.builder()
                        .response(responseDTO)
                        .responseMsg("Money transfer request failed")
                        .statusModel(new StatusModel(400, REQUEST_FAILED))
                        .build()
        );
    }

    private ResponseEntity<APIResponseDTO> prepareSuccessResponse(TransactionResponseDTO responseDTO) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(responseDTO)
                        .responseMsg("Money transfer request processed")
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }
}
