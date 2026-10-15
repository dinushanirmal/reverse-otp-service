package com.example.reverseotp.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Instant;

public class GenerateTokenResponse {

    private String requestId;
    private String token;
    private String phoneNumber;

    @JsonAlias({"receiverNumber", "destinationNumber", "targetNumber"})
    private String recipientNumber;

    private String smsContent;
    private Instant expiresAt;

    public GenerateTokenResponse() {
    }

    public GenerateTokenResponse(String requestId, String token, String phoneNumber, String recipientNumber, String smsContent, Instant expiresAt) {
        this.requestId = requestId;
        this.token = token;
        this.phoneNumber = phoneNumber;
        this.recipientNumber = recipientNumber;
        this.smsContent = smsContent;
        this.expiresAt = expiresAt;
    }

    public GenerateTokenResponse(String requestId, String token, String phoneNumber, String smsContent, Instant expiresAt) {
        this(requestId, token, phoneNumber, null, smsContent, expiresAt);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
