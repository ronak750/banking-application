package com.transactions.message.dto;

public class StatusModel {
    private int statusCode;
    private String statusMsg;

    public StatusModel() {
    }

    public StatusModel(int statusCode, String statusMsg) {
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }
}
