package com.example.reverseotp.controller;

import com.example.reverseotp.dto.request.GenerateTokenRequest;
import com.example.reverseotp.dto.response.GenerateTokenResponse;
import com.example.reverseotp.dto.response.TokenStatusResponse;
import com.example.reverseotp.service.TokenService;
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
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateTokenResponse> generateToken(@Valid @RequestBody GenerateTokenRequest request) {
        GenerateTokenResponse response = tokenService.generateToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{requestId}/status")
    public ResponseEntity<TokenStatusResponse> getStatus(@PathVariable String requestId) {
        TokenStatusResponse response = tokenService.getTokenStatus(requestId);
        return ResponseEntity.ok(response);
    }
}
