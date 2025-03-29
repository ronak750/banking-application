package com.transactions.transactions.exception;

import lombok.Getter;

// Custom exception to hold the original error details
@Getter
public class ApiException extends RuntimeException {
    // Getters
    private final int statusCode;
    private final String errorMessage;
    private final String errorCode;

    public ApiException(int statusCode, String errorMessage, String errorCode) {
        super("API Error: " + statusCode + " - " + errorMessage + " (Code: " + errorCode + ")");
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

}
