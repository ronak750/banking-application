package com.transactions.transactions.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactions.transactions.dtos.ConnectorApiErrorResponse;
import com.transactions.transactions.dtos.ErrorResponseDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.transactions.transactions.utils.Constants.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(USER_NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(INVALID_FIELD, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(InvalidFieldException ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(INVALID_FIELD, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionRequestFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleTransactionRequestFailedException(TransactionRequestFailedException ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(TRANSACTION_REQUEST_FAILED, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", ex.getErrorCode());
        errorResponse.put("message", ex.getErrorMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
