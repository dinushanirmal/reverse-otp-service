package com.example.reverseotp.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Telco webhook payload containing received MO SMS details")
public class TelcoWebhookRequest {

    @NotBlank(message = "Sender phone number is required")
    @JsonAlias({"from", "senderNumber", "msisdn", "mobile"})
    @Schema(description = "Phone number that sent the SMS", example = "+94771234567")
    private String sender;

    @NotBlank(message = "Message content is required")
    @JsonAlias({"content", "text", "body", "sms"})
    @Schema(description = "Raw SMS text received by the telco gateway", example = "A1B2C3D4E5F67890-849201")
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
