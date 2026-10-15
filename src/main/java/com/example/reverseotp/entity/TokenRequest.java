package com.example.reverseotp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "token_requests")
public class TokenRequest {

    @Id
    @Column(name = "request_id", length = 16, nullable = false, updatable = false)
    private String requestId;

    @Column(name = "phone_number", length = 30, nullable = false)
    private String phoneNumber;

    @Column(name = "token", length = 160, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "incoming_message_id")
    private Long incomingMessageId;

    public TokenRequest() {
    }

    public TokenRequest(String requestId, String phoneNumber, String token, TokenStatus status,
                        Instant expiresAt, Instant createdAt) {
        this.requestId = requestId;
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Long getIncomingMessageId() {
        return incomingMessageId;
    }

    public void setIncomingMessageId(Long incomingMessageId) {
        this.incomingMessageId = incomingMessageId;
    }
}
