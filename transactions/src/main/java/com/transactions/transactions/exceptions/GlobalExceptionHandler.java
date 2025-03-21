package com.transactions.transactions.exceptions;

import com.transactions.transactions.dtos.ErrorResponseDto;
import feign.FeignException;
import io.micrometer.core.instrument.config.validate.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDto> handleFeignException(FeignException e) {
        if (e.status() == 400) {
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(RESOURCE_NOT_FOUND, e.getMessage());
            return ResponseEntity.badRequest().body(errorResponseDto);
        } else {
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(INTERNAL_SERVER_ERROR, e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponseDto);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
