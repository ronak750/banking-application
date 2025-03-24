package com.transactions.api.gateway.exceptions;

public class CustomUnauthorizedException extends Exception{
    public CustomUnauthorizedException(String message) {
        super(message);
    }
}
