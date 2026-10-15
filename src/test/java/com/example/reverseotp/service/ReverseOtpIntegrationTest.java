package com.example.reverseotp.service;

import com.example.reverseotp.dto.request.GenerateTokenRequest;
import com.example.reverseotp.dto.request.TelcoWebhookRequest;
import com.example.reverseotp.dto.response.GenerateTokenResponse;
import com.example.reverseotp.dto.response.TokenStatusResponse;
import com.example.reverseotp.dto.response.WebhookProcessResponse;
import com.example.reverseotp.entity.IncomingMessage;
import com.example.reverseotp.entity.MatchStatus;
import com.example.reverseotp.entity.TokenRequest;
import com.example.reverseotp.entity.TokenStatus;
import com.example.reverseotp.repository.IncomingMessageRepository;
import com.example.reverseotp.repository.TokenRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class ReverseOtpIntegrationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private TokenRequestRepository tokenRequestRepository;

    @Autowired
    private IncomingMessageRepository incomingMessageRepository;

    @BeforeEach
    void setUp() {
        tokenRequestRepository.deleteAll();
        incomingMessageRepository.deleteAll();
    }

    @Test
    void testCompleteSuccessfulFlow() {
        // 1. Generate Token
        String phone = "+94771234567";
        GenerateTokenResponse tokenResp = tokenService.generateToken(new GenerateTokenRequest(phone));

        assertNotNull(tokenResp);
        assertNotNull(tokenResp.getRequestId());
        assertEquals(16, tokenResp.getRequestId().length());
        assertTrue(tokenResp.getToken().startsWith(tokenResp.getRequestId()));
        assertEquals(phone, tokenResp.getPhoneNumber());

        // Check TokenRequest table
        TokenRequest storedRequest = tokenRequestRepository.findByRequestId(tokenResp.getRequestId()).orElse(null);
        assertNotNull(storedRequest);
        assertEquals(TokenStatus.PENDING, storedRequest.getStatus());
        assertNull(storedRequest.getVerifiedAt());

        // Initial status check should be PENDING
        TokenStatusResponse statusResp1 = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.PENDING, statusResp1.getStatus());
        assertEquals(phone, statusResp1.getPhoneNumber());

        // 2. Telco sends webhook for matching SMS (first 16 chars is the requestId)
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("94771234567", tokenResp.getToken());
        WebhookProcessResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertTrue(webhookResp.isMatched());
        assertEquals(MatchStatus.APPROVED, webhookResp.getMatchStatus());
        assertEquals(tokenResp.getRequestId(), webhookResp.getExtractedRequestId());

        // Check incoming_messages table
        List<IncomingMessage> incomingList = incomingMessageRepository.findAll();
        assertEquals(1, incomingList.size());
        IncomingMessage incomingMsg = incomingList.get(0);
        assertTrue(incomingMsg.isMatched());
        assertEquals(MatchStatus.APPROVED, incomingMsg.getMatchStatus());

        // 3. Check status API after webhook -> should be APPROVED with audit data
        TokenStatusResponse statusResp2 = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.APPROVED, statusResp2.getStatus());
        assertEquals(phone, statusResp2.getPhoneNumber());
        assertNotNull(statusResp2.getVerifiedAt());

        // Check audit data in token_requests table
        TokenRequest verifiedToken = tokenRequestRepository.findByRequestId(tokenResp.getRequestId()).orElseThrow();
        assertEquals(incomingMsg.getId(), verifiedToken.getIncomingMessageId());
        assertNotNull(verifiedToken.getVerifiedAt());
    }

    @Test
    void testWebhookPhoneMismatchFails() {
        // Generate Token for phone A
        GenerateTokenResponse tokenResp = tokenService.generateToken(new GenerateTokenRequest("+94771111111"));

        // Webhook comes from phone B with the same token
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("+94772222222", tokenResp.getToken());
        WebhookProcessResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertFalse(webhookResp.isMatched());
        assertEquals(MatchStatus.PHONE_MISMATCH, webhookResp.getMatchStatus());

        // Status should still be PENDING
        TokenStatusResponse statusResp = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.PENDING, statusResp.getStatus());

        // Incoming message was still stored
        assertEquals(1, incomingMessageRepository.count());
    }

    @Test
    void testWebhookExpiredToken() {
        // Generate Token
        GenerateTokenResponse tokenResp = tokenService.generateToken(new GenerateTokenRequest("+94771234567"));

        // Manually set expiry in the past
        TokenRequest req = tokenRequestRepository.findByRequestId(tokenResp.getRequestId()).orElseThrow();
        req.setExpiresAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        tokenRequestRepository.save(req);

        // Webhook arrives
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("+94771234567", tokenResp.getToken());
        WebhookProcessResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertFalse(webhookResp.isMatched());
        assertEquals(MatchStatus.EXPIRED, webhookResp.getMatchStatus());

        // Status should now be EXPIRED
        TokenStatusResponse statusResp = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.EXPIRED, statusResp.getStatus());
    }

    @Test
    void testWebhookUnknownRequestId() {
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("+94771234567", "UNKNOWNREQUESTID123-555");
        WebhookProcessResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertFalse(webhookResp.isMatched());
        assertEquals(MatchStatus.TOKEN_NOT_FOUND, webhookResp.getMatchStatus());

        // Message should be stored in incoming_messages
        assertEquals(1, incomingMessageRepository.count());
    }
}
