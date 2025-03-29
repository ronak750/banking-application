package com.transactions.gatewayconnector.controller;

import com.transactions.gatewayconnector.dto.*;
import com.transactions.gatewayconnector.dto.request.NeftTransferRequestDto;
import com.transactions.gatewayconnector.dto.request.UPIAndPINRequestDto;
import com.transactions.gatewayconnector.dto.request.UPITransferRequestDTO;
import com.transactions.gatewayconnector.dto.request.WalletTransferRequestDto;
import com.transactions.gatewayconnector.service.IAdapter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.transactions.gatewayconnector.constant.Constants.*;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/connector-gateway")
public class ConnectorGatewayController {

    private final IAdapter adapter;

    @PostMapping("/transfer-wallet-money")
    public ResponseEntity<APIResponseDTO> transferWalletMoney(@Valid
                                                          @RequestBody
                                                              WalletTransferRequestDto walletTransferRequestDto) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.transferWalletMoney(walletTransferRequestDto))
                        .responseMsg(TRANSACTION_SUCCESSFUL)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/transfer-upi-money")
    public ResponseEntity<APIResponseDTO> transferUpiMoney(@Valid
                                                      @RequestBody
                                                           UPITransferRequestDTO upiTransferRequestDTO) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.transferUPIMoney(upiTransferRequestDTO))
                        .responseMsg(BALANCE_SUCCESSFULLY_FETCHED)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/transfer-neft-money")
    public ResponseEntity<APIResponseDTO> transferNEFTMoney(@RequestBody List<NeftTransferRequestDto> neftTransferRequestDtoList) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.transferNEFTMoney(neftTransferRequestDtoList))
                        .responseMsg(BALANCE_SUCCESSFULLY_FETCHED)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/upi-balance")
    public ResponseEntity<APIResponseDTO> checkUPIBalance(@Valid @RequestBody UPIAndPINRequestDto upiAndPINRequestDto) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.checkUPIBalance(upiAndPINRequestDto.upiId(), upiAndPINRequestDto.pin()))
                        .responseMsg(BALANCE_SUCCESSFULLY_FETCHED)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/upi-validation")
    public ResponseEntity<APIResponseDTO> validateUPI(@NotEmpty @RequestBody String upiId) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.validateUPI(upiId))
                        .responseMsg(BALANCE_SUCCESSFULLY_FETCHED)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
    }

    @PostMapping("/wallet-balance")
     public ResponseEntity<APIResponseDTO> checkWalletBalance(@NotEmpty @RequestBody String walletId) {
        return ResponseEntity.ok(
                APIResponseDTO.builder()
                        .response(adapter.checkWalletBalance(walletId))
                        .responseMsg(BALANCE_SUCCESSFULLY_FETCHED)
                        .statusModel(new StatusModel(200, SUCCESS))
                        .build()
        );
     }
}

