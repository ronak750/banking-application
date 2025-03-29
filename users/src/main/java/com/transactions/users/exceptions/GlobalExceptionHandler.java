package com.transactions.users.exceptions;

import com.transactions.users.dtos.commons.ErrorDetails;
import com.transactions.users.dtos.commons.ErrorResponseDto;
import jakarta.validation.ValidationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static com.transactions.users.utils.Constants.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                REQUEST_FAILED_MSG,
                new ErrorDetails(USER_NOT_FOUND, ex.getMessage())
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateUserException(DuplicateUserException ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                REGISTRATION_FAILED_MSG,
                new ErrorDetails(USER_ALREADY_REGISTERED, ex.getMessage())
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = String.join("; ", getListOfInvalidFieldsErrors(ex));

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                INVALID_FIELD_MSG,
                new ErrorDetails(INVALID_FIELD, errorMessage)
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(ValidationException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                REQUEST_FAILED_MSG,
                new ErrorDetails(UNKNOWN, ex.getMessage())
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                REQUEST_FAILED_MSG,
                new ErrorDetails(INTERNAL_SERVER_ERROR, ex.getMessage())
        );
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private List<String> getListOfInvalidFieldsErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
    }
}
