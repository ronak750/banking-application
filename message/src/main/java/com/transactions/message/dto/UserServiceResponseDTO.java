package com.transactions.message.dto;

public class UserServiceResponseDTO {
    private StatusModel statusModel;
    private String responseMsg;
    private UserResponseDTO response;

    public StatusModel getStatusModel() {
        return statusModel;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setStatusModel(StatusModel statusModel) {
        this.statusModel = statusModel;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void setResponse(UserResponseDTO response) {
        this.response = response;
    }

    public UserResponseDTO getResponse() {
        return response;
    }

    public UserServiceResponseDTO(
            StatusModel statusModel,
            String responseMsg,
            UserResponseDTO response
    ) {
        this.statusModel = statusModel;
        this.responseMsg = responseMsg;
        this.response = response;
    }
}
