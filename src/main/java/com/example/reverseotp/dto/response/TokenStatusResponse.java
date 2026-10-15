package com.example.reverseotp.dto.response;

import com.example.reverseotp.entity.TokenStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Verification status and details of a requested token")
public class TokenStatusResponse {

    @Schema(description = "16-character unique Request ID", example = "A1B2C3D4E5F67890")
    private String requestId;

    @Schema(description = "Associated mobile number", example = "+94771234567")
    private String phoneNumber;

    @Schema(description = "Current token verification status", example = "APPROVED")
    private TokenStatus status;

    @Schema(description = "Timestamp when token request was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when token expires")
    private Instant expiresAt;

    @Schema(description = "Timestamp when token was successfully verified (null if pending/expired)")
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
