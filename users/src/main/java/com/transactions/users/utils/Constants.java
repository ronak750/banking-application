package com.transactions.users.utils;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final String USER_NOT_FOUND = "400_USER_NOT_FOUND";
    public static final String INTERNAL_SERVER_ERROR = "500_INTERNAL_SERVER_ERROR";
    public static final String USER_ALREADY_REGISTERED = "409_USER_ALREADY_REGISTERED";
    public static final String INVALID_FIELD = "400_INVALID_FIELD";
    public static final String UNKNOWN = "UNKNOWN";


    public static final String REQUEST_FAILED_MSG = "Your request cannot be processed";
    public static final String INVALID_FIELD_MSG = "Your request cannot be processed as some of the input fields are invalid";
    public static final String REGISTRATION_FAILED_MSG = "User registration failed";
}
