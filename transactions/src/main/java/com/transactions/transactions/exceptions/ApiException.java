package com.transactions.transactions.exceptions;

// Custom exception to hold the original error details
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorMessage;
    private final String errorCode;

    public ApiException(int statusCode, String errorMessage, String errorCode) {
        super("API Error: " + statusCode + " - " + errorMessage + " (Code: " + errorCode + ")");
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    // Getters
    public int getStatusCode() { return statusCode; }
    public String getErrorMessage() { return errorMessage; }
    public String getErrorCode() { return errorCode; }
}
