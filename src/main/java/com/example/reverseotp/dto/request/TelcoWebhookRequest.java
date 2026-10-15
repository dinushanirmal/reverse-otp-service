package com.example.reverseotp.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class TelcoWebhookRequest {

    @NotBlank(message = "Sender phone number is required")
    @JsonAlias({"from", "senderNumber", "msisdn", "mobile"})
    private String sender;

    @NotBlank(message = "Message content is required")
    @JsonAlias({"content", "text", "body", "sms"})
    private String message;

    public TelcoWebhookRequest() {
    }

    public TelcoWebhookRequest(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
