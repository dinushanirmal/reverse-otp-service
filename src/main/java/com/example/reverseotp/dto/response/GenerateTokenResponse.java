package com.example.reverseotp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Response payload after generating a token")
public class GenerateTokenResponse {

    @Schema(description = "16-character unique Request ID", example = "A1B2C3D4E5F67890")
    private String requestId;

    @Schema(description = "Full token (under 160 characters for SMS)", example = "A1B2C3D4E5F67890-849201")
    private String token;

    @Schema(description = "Normalized phone number", example = "+94771234567")
    private String phoneNumber;

    @Schema(description = "Suggested SMS message content for recipient", example = "Your verification token is: A1B2C3D4E5F67890-849201")
    private String smsContent;

    @Schema(description = "Token expiration timestamp")
    private Instant expiresAt;

    public GenerateTokenResponse() {
    }

    public GenerateTokenResponse(String requestId, String token, String phoneNumber, String smsContent, Instant expiresAt) {
        this.requestId = requestId;
        this.token = token;
        this.phoneNumber = phoneNumber;
        this.smsContent = smsContent;
        this.expiresAt = expiresAt;
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
