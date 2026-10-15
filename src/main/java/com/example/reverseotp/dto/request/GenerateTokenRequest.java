package com.example.reverseotp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload to generate a new reverse OTP token")
public class GenerateTokenRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format. Must contain 7 to 15 digits, optional leading +")
    @Schema(description = "Target mobile number (E.164 or national format)", example = "+94771234567")
    private String phoneNumber;

    public GenerateTokenRequest() {
    }

    public GenerateTokenRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
