package com.example.reverseotp.controller;

import com.example.reverseotp.dto.request.GenerateTokenRequest;
import com.example.reverseotp.dto.response.GenerateTokenResponse;
import com.example.reverseotp.dto.response.TokenStatusResponse;
import com.example.reverseotp.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tokens")
@Tag(name = "Token Management", description = "APIs for token generation and status verification")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate a new reverse OTP token",
            description = "Creates a unique token with a 16-character request ID prefix and persists it in token_requests.")
    @ApiResponse(responseCode = "201", description = "Token generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid phone number format")
    public ResponseEntity<GenerateTokenResponse> generateToken(@Valid @RequestBody GenerateTokenRequest request) {
        GenerateTokenResponse response = tokenService.generateToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{requestId}/status")
    @Operation(summary = "Get token verification status",
            description = "Retrieves current status (PENDING, APPROVED, EXPIRED) and mobile number for a given 16-character request ID.")
    @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Token request not found")
    public ResponseEntity<TokenStatusResponse> getStatus(
            @Parameter(description = "16-character Request ID", example = "A1B2C3D4E5F67890")
            @PathVariable String requestId) {
        TokenStatusResponse response = tokenService.getTokenStatus(requestId);
        return ResponseEntity.ok(response);
    }
}
