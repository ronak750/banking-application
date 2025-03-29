package com.transactions.transactions.controllers;

import com.transactions.transactions.dto.TransactionStatus;
import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import com.transactions.transactions.dto.request.WalletTransactionResponseDTO;
import com.transactions.transactions.dto.request.WalletTransferRequestDto;
import com.transactions.transactions.dto.response.BalanceResponseDTO;
import com.transactions.transactions.services.WalletService;
import com.transactions.transactions.utils.UtilClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
@RequestMapping("api/v1/wallets")
@AllArgsConstructor
@Validated
public class WalletController {

    private WalletService walletService;

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<APIResponseDTO> getBalance(@Min(value = 1) @PathVariable Long walletId) {
        BigDecimal balance = walletService.getBalance(walletId).setScale(2, RoundingMode.HALF_UP);
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(new BalanceResponseDTO(balance))
                        .responseMsg("Wallet balance successfully fetched")
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }


    @PostMapping("/transfer")
    public ResponseEntity<APIResponseDTO> transferMoney(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WalletTransferRequestDto transferRequest
    ) {
        var response = walletService.transferMoney(transferRequest, userId);

        return prepareTransactionResponse(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<APIResponseDTO> getTransactions(
            @PathVariable String walletId,
            @RequestParam (required = false, defaultValue = "10") String count,
            @RequestParam (required = false, defaultValue = "1") String page
    ) {
        Pageable pageable = UtilClass.getPageable(page, count);
        List<WalletTransactionResponseDTO> transactions = walletService.getTransactions(walletId, pageable);
        return prepareGetWalletTransactionsResponse(transactions);
    }

    private ResponseEntity<APIResponseDTO> prepareGetWalletTransactionsResponse(List<WalletTransactionResponseDTO> transactions) {
        if(transactions.isEmpty()) {
            return new ResponseEntity<>(
                    APIResponseDTO.builder()
                            .statusModel(new StatusModel(200, SUCCESS))
                            .responseMsg("No Wallet transactions available for provided filters")
                            .response(transactions)
                            .build()  ,
                    HttpStatus.OK
            );
        }
        else {
            return new ResponseEntity<>(
                    APIResponseDTO.builder()
                            .statusModel(new StatusModel(200, SUCCESS))
                            .response(transactions)
                            .responseMsg("Wallet transactions fetched successfully")
                            .build()  ,
                    HttpStatus.OK
            );
        }
    }

    private ResponseEntity<APIResponseDTO> prepareTransactionResponse(WalletTransactionResponseDTO response) {
        if(response.status().equals(TransactionStatus.SUCCESS))
            return prepareSuccessResponse(response);
        else
            return prepareFailureResponse(response);
    }

    private ResponseEntity<APIResponseDTO> prepareFailureResponse(WalletTransactionResponseDTO responseDTO) {
        return ResponseEntity.badRequest().body(
                APIResponseDTO.builder()
                        .response(responseDTO)
                        .responseMsg("Money transfer request failed")
                        .statusModel(new StatusModel(400, REQUEST_FAILED))
                        .build()
        );
    }

    private ResponseEntity<APIResponseDTO> prepareSuccessResponse(WalletTransactionResponseDTO responseDTO) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(responseDTO)
                        .responseMsg("Money transfer request processed")
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }
}