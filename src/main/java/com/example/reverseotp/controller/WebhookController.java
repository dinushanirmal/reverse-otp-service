package com.example.reverseotp.controller;

import com.example.reverseotp.dto.request.TelcoWebhookRequest;
import com.example.reverseotp.dto.response.TelcoWebhookResponse;
import com.example.reverseotp.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/telco/sms")
    public ResponseEntity<TelcoWebhookResponse> receiveSms(@Valid @RequestBody TelcoWebhookRequest request) {
        TelcoWebhookResponse response = webhookService.processIncomingSms(request);
        return ResponseEntity.ok(response);
    }
}
