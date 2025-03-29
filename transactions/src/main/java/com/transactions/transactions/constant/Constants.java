package com.transactions.transactions.constant;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String INVALID_FIELD = "400_INVALID_FIELD";
    public static final String TRANSACTION_REQUEST_FAILED = "Transaction Request Failed";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String SUCCESS = "SUCCESS";
    public static final String REQUEST_FAILED = "REQUEST_FAILED";

    public static final String INVALID_AMOUNT_TRANSFER_ERROR_MSG = "Invalid amount to transfer, amount should be less than 1 Lakhs";
    public static final String SOMETHING_WENT_WRONG_MSG = "Something went wrong";
    public static final String SAME_ACCOUNT_TRANSFER_ERROR_MSG = "Money cannot be transferred to same account";
    public static final String DUPLICATE_TRANSACTION_ERROR_MSG = "Duplicate transaction request received";
}
