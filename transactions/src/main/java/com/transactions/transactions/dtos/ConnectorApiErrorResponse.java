package com.transactions.transactions.dtos;

//import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;

public class ConnectorApiErrorResponse {
    private String errorCode;
    private String message;
    private HttpStatus statusCode;

    // Getters and setters
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public HttpStatus getStatusCode() { return statusCode; }
    public void setStatusCode(HttpStatus statusCode) { this.statusCode = statusCode; }
}
