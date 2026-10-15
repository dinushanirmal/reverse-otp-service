package com.example.reverseotp.dto.response;

public class TelcoWebhookResponse {

    private String status;
    private String message;

    public TelcoWebhookResponse() {
        this.status = "RECEIVED";
        this.message = "SMS received successfully";
    }

    public TelcoWebhookResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
