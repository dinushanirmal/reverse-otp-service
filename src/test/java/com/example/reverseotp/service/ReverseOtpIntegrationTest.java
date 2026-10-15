package com.example.reverseotp.service;

import com.example.reverseotp.dto.request.GenerateTokenRequest;
import com.example.reverseotp.dto.request.TelcoWebhookRequest;
import com.example.reverseotp.dto.response.GenerateTokenResponse;
import com.example.reverseotp.dto.response.TelcoWebhookResponse;
import com.example.reverseotp.dto.response.TokenStatusResponse;
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
        // 1. Generate Token (64-char pure hex)
        String phone = "+94771234567";
        GenerateTokenResponse tokenResp = tokenService.generateToken(new GenerateTokenRequest(phone));

        assertNotNull(tokenResp);
        assertNotNull(tokenResp.getRequestId());
        assertEquals(16, tokenResp.getRequestId().length());
        assertEquals(64, tokenResp.getToken().length());
        assertTrue(tokenResp.getToken().matches("^[0-9A-F]{64}$"));
        assertTrue(tokenResp.getToken().startsWith(tokenResp.getRequestId()));
        assertEquals(phone, tokenResp.getPhoneNumber());
        assertEquals(tokenResp.getToken(), tokenResp.getSmsContent());

        // Check TokenRequest table in H2
        TokenRequest storedRequest = tokenRequestRepository.findByRequestId(tokenResp.getRequestId()).orElse(null);
        assertNotNull(storedRequest);
        assertEquals(TokenStatus.PENDING, storedRequest.getStatus());
        assertNull(storedRequest.getVerifiedAt());

        // Initial status check should be PENDING
        TokenStatusResponse statusResp1 = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.PENDING, statusResp1.getStatus());
        assertEquals(phone, statusResp1.getPhoneNumber());

        // 2. Telco sends webhook for matching SMS (receives simple acknowledgement)
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("94771234567", tokenResp.getToken());
        TelcoWebhookResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertNotNull(webhookResp);
        assertEquals("RECEIVED", webhookResp.getStatus());

        // Check incoming_messages table records internal match outcome
        List<IncomingMessage> incomingList = incomingMessageRepository.findAll();
        assertEquals(1, incomingList.size());
        IncomingMessage incomingMsg = incomingList.get(0);
        assertTrue(incomingMsg.isMatched());
        assertEquals(MatchStatus.APPROVED, incomingMsg.getMatchStatus());
        assertEquals(tokenResp.getRequestId(), incomingMsg.getExtractedRequestId());

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
    void testWebhookPhoneMismatchFailsInternalMatch() {
        // Generate Token for phone A
        GenerateTokenResponse tokenResp = tokenService.generateToken(new GenerateTokenRequest("+94771111111"));

        // Webhook comes from phone B with the same token
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("+94772222222", tokenResp.getToken());
        TelcoWebhookResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        // Telco receives standard receipt ACK
        assertEquals("RECEIVED", webhookResp.getStatus());

        // Status should still be PENDING in token_requests
        TokenStatusResponse statusResp = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.PENDING, statusResp.getStatus());

        // Incoming message was stored with PHONE_MISMATCH
        List<IncomingMessage> messages = incomingMessageRepository.findAll();
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).isMatched());
        assertEquals(MatchStatus.PHONE_MISMATCH, messages.get(0).getMatchStatus());
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
        TelcoWebhookResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertEquals("RECEIVED", webhookResp.getStatus());

        // Status should now be EXPIRED
        TokenStatusResponse statusResp = tokenService.getTokenStatus(tokenResp.getRequestId());
        assertEquals(TokenStatus.EXPIRED, statusResp.getStatus());

        // Incoming message recorded as EXPIRED
        List<IncomingMessage> messages = incomingMessageRepository.findAll();
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).isMatched());
        assertEquals(MatchStatus.EXPIRED, messages.get(0).getMatchStatus());
    }

    @Test
    void testWebhookUnknownRequestId() {
        TelcoWebhookRequest webhookReq = new TelcoWebhookRequest("+94771234567", "0123456789ABCDEF0123456789ABCDEF");
        TelcoWebhookResponse webhookResp = webhookService.processIncomingSms(webhookReq);

        assertEquals("RECEIVED", webhookResp.getStatus());

        // Message should be stored in incoming_messages
        List<IncomingMessage> messages = incomingMessageRepository.findAll();
        assertEquals(1, messages.size());
        assertFalse(messages.get(0).isMatched());
        assertEquals(MatchStatus.TOKEN_NOT_FOUND, messages.get(0).getMatchStatus());
    }
}
