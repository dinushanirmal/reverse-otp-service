package com.example.reverseotp.controller;

import com.example.reverseotp.dto.request.TelcoWebhookRequest;
import com.example.reverseotp.dto.response.WebhookProcessResponse;
import com.example.reverseotp.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Telco Webhook", description = "Endpoints called by Telco SMS gateways for Mobile-Originated (MO) SMS")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/telco/sms")
    @Operation(summary = "Receive incoming SMS from telco provider",
            description = "Stores incoming message in incoming_messages, matches the first 16 chars with pending token requests, and updates approval status.")
    @ApiResponse(responseCode = "200", description = "Webhook processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public ResponseEntity<WebhookProcessResponse> receiveSms(@Valid @RequestBody TelcoWebhookRequest request) {
        WebhookProcessResponse response = webhookService.processIncomingSms(request);
        return ResponseEntity.ok(response);
    }
}
