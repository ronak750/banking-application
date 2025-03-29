package com.transactions.gatewayconnector.exception;

import com.transactions.gatewayconnector.dto.APIResponseDTO;
import com.transactions.gatewayconnector.dto.StatusModel;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.transactions.gatewayconnector.constant.Constants.*;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = String.join("; ", getListOfInvalidFieldsErrors(ex));
        return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .statusModel(new StatusModel(HttpStatus.BAD_REQUEST.value(), INVALID_FIELD))
                        .responseMsg(errorMessage)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponseDTO> handleValidationExceptions(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                APIResponseDTO.builder()
                        .responseMsg(ex.getMessage())
                        .statusModel(new StatusModel(400, INVALID_FIELD))
                        .build()
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponseDTO> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(
                APIResponseDTO.builder()
                        .responseMsg(SOMETHING_WENT_WRONG_MSG)
                        .statusModel(new StatusModel(500, INTERNAL_SERVER_ERROR))
                        .build()
        );
    }

    private List<String> getListOfInvalidFieldsErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
