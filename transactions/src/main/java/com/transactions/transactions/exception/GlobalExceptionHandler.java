package com.transactions.transactions.exception;

import com.transactions.transactions.dto.commons.APIResponseDTO;
import com.transactions.transactions.dto.commons.StatusModel;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.transactions.transactions.constant.Constants.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = String.join("; ", getListOfInvalidFieldsErrors(ex));

        return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .responseMsg(errorMessage)
                        .statusModel(new StatusModel(400, INVALID_FIELD))
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<APIResponseDTO> handleValidationExceptions(InvalidFieldException ex) {
        return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .responseMsg(ex.getMessage())
                        .statusModel(new StatusModel(400, INVALID_FIELD))
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<APIResponseDTO> handleApiException(ApiException ex) {
        return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .responseMsg(ex.getErrorMessage())
                        .statusModel(new StatusModel(ex.getStatusCode(), ex.getErrorCode()))
                        .build(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponseDTO> handleException(Exception ex) {
        return new ResponseEntity<>(
                APIResponseDTO.builder()
                        .responseMsg(SOMETHING_WENT_WRONG_MSG)
                        .statusModel(new StatusModel(500, INTERNAL_SERVER_ERROR))
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private List<String> getListOfInvalidFieldsErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}

