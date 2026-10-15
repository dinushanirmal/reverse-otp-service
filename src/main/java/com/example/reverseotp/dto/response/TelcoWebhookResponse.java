package com.example.reverseotp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple acknowledgment response for telco SMS webhook")
public class TelcoWebhookResponse {

    @Schema(description = "Processing status", example = "RECEIVED")
    private String status;

    @Schema(description = "Message description", example = "SMS received successfully")
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
