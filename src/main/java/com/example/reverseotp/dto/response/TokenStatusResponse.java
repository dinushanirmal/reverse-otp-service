package com.example.reverseotp.dto.response;

import com.example.reverseotp.entity.TokenStatus;
import java.time.Instant;

public class TokenStatusResponse {

    private String requestId;
    private String phoneNumber;
    private TokenStatus status;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant verifiedAt;

    public TokenStatusResponse() {
    }

    public TokenStatusResponse(String requestId, String phoneNumber, TokenStatus status,
                               Instant createdAt, Instant expiresAt, Instant verifiedAt) {
        this.requestId = requestId;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
