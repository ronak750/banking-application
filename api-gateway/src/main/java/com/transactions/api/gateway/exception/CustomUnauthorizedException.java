package com.transactions.api.gateway.exception;

public class CustomUnauthorizedException extends Exception{
    public CustomUnauthorizedException(String message) {
        super(message);
    }
}
