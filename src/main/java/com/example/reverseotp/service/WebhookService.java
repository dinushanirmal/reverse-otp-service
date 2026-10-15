package com.example.reverseotp.service;

import com.example.reverseotp.dto.request.TelcoWebhookRequest;
import com.example.reverseotp.dto.response.WebhookProcessResponse;
import com.example.reverseotp.entity.IncomingMessage;
import com.example.reverseotp.entity.MatchStatus;
import com.example.reverseotp.entity.TokenRequest;
import com.example.reverseotp.entity.TokenStatus;
import com.example.reverseotp.repository.IncomingMessageRepository;
import com.example.reverseotp.repository.TokenRequestRepository;
import com.example.reverseotp.util.PhoneNumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int REQUEST_ID_LENGTH = 16;

    private final IncomingMessageRepository incomingMessageRepository;
    private final TokenRequestRepository tokenRequestRepository;

    public WebhookService(IncomingMessageRepository incomingMessageRepository,
                          TokenRequestRepository tokenRequestRepository) {
        this.incomingMessageRepository = incomingMessageRepository;
        this.tokenRequestRepository = tokenRequestRepository;
    }

    /**
     * Processes incoming telco MO SMS webhook:
     * 1. Stores raw message in incoming_messages table.
     * 2. Extracts request ID from first 16 characters.
     * 3. Checks for matching token request in token_requests for same request ID and phone number.
     * 4. If matched and pending, marks as APPROVED with audit details.
     */
    @Transactional
    public WebhookProcessResponse processIncomingSms(TelcoWebhookRequest request) {
        String rawContent = request.getMessage() != null ? request.getMessage().trim() : "";
        String senderNumber = PhoneNumberUtil.normalize(request.getSender());

        // Derive 16-character request ID from the first 16 characters of content
        String extractedRequestId = extractRequestId(rawContent);

        Instant receivedAt = Instant.now();

        // 1. First persist in incoming_messages table
        IncomingMessage incomingMessage = new IncomingMessage(
                senderNumber,
                rawContent,
                extractedRequestId,
                false,
                MatchStatus.TOKEN_NOT_FOUND,
                receivedAt
        );
        incomingMessage = incomingMessageRepository.save(incomingMessage);
        log.info("Saved incoming message id={}, sender={}, extractedRequestId={}",
                incomingMessage.getId(), senderNumber, extractedRequestId);

        // 2. Validate extracted request ID
        if (extractedRequestId == null || extractedRequestId.length() < REQUEST_ID_LENGTH) {
            incomingMessage.setMatched(false);
            incomingMessage.setMatchStatus(MatchStatus.TOKEN_NOT_FOUND);
            incomingMessageRepository.save(incomingMessage);
            return new WebhookProcessResponse(
                    incomingMessage.getId(),
                    extractedRequestId,
                    false,
                    MatchStatus.TOKEN_NOT_FOUND,
                    "Message content must contain at least 16 characters for Request ID"
            );
        }

        // 3. Check for matching token request
        Optional<TokenRequest> tokenOpt = tokenRequestRepository.findByRequestId(extractedRequestId);

        if (tokenOpt.isEmpty()) {
            incomingMessage.setMatched(false);
            incomingMessage.setMatchStatus(MatchStatus.TOKEN_NOT_FOUND);
            incomingMessageRepository.save(incomingMessage);
            log.warn("No token request found for requestId={}", extractedRequestId);

            return new WebhookProcessResponse(
                    incomingMessage.getId(),
                    extractedRequestId,
                    false,
                    MatchStatus.TOKEN_NOT_FOUND,
                    "No token request found for the provided Request ID"
            );
        }

        TokenRequest tokenRequest = tokenOpt.get();

        // Check if token has expired
        if (receivedAt.isAfter(tokenRequest.getExpiresAt())) {
            tokenRequest.setStatus(TokenStatus.EXPIRED);
            tokenRequestRepository.save(tokenRequest);

            incomingMessage.setMatched(false);
            incomingMessage.setMatchStatus(MatchStatus.EXPIRED);
            incomingMessageRepository.save(incomingMessage);
            log.warn("Token request {} has expired", extractedRequestId);

            return new WebhookProcessResponse(
                    incomingMessage.getId(),
                    extractedRequestId,
                    false,
                    MatchStatus.EXPIRED,
                    "Token request has expired"
            );
        }

        // Check if already approved (idempotency check)
        if (tokenRequest.getStatus() == TokenStatus.APPROVED) {
            incomingMessage.setMatched(true);
            incomingMessage.setMatchStatus(MatchStatus.ALREADY_APPROVED);
            incomingMessageRepository.save(incomingMessage);
            log.info("Token request {} is already approved", extractedRequestId);

            return new WebhookProcessResponse(
                    incomingMessage.getId(),
                    extractedRequestId,
                    true,
                    MatchStatus.ALREADY_APPROVED,
                    "Token was already verified and approved"
            );
        }

        // Verify phone number match
        if (!PhoneNumberUtil.matches(tokenRequest.getPhoneNumber(), senderNumber)) {
            incomingMessage.setMatched(false);
            incomingMessage.setMatchStatus(MatchStatus.PHONE_MISMATCH);
            incomingMessageRepository.save(incomingMessage);
            log.warn("Phone number mismatch for requestId={}: expected={}, actual={}",
                    extractedRequestId, tokenRequest.getPhoneNumber(), senderNumber);

            return new WebhookProcessResponse(
                    incomingMessage.getId(),
                    extractedRequestId,
                    false,
                    MatchStatus.PHONE_MISMATCH,
                    "Sender phone number does not match registered phone number"
            );
        }

        // Token is valid and pending! Mark as APPROVED with audit details
        tokenRequest.setStatus(TokenStatus.APPROVED);
        tokenRequest.setVerifiedAt(receivedAt);
        tokenRequest.setIncomingMessageId(incomingMessage.getId());
        tokenRequestRepository.save(tokenRequest);

        incomingMessage.setMatched(true);
        incomingMessage.setMatchStatus(MatchStatus.APPROVED);
        incomingMessageRepository.save(incomingMessage);

        log.info("Successfully approved token request {} for phone {}", extractedRequestId, senderNumber);

        return new WebhookProcessResponse(
                incomingMessage.getId(),
                extractedRequestId,
                true,
                MatchStatus.APPROVED,
                "Token matched and approved successfully"
        );
    }

    private String extractRequestId(String content) {
        if (content == null || content.length() < REQUEST_ID_LENGTH) {
            return null;
        }
        return content.substring(0, REQUEST_ID_LENGTH).toUpperCase();
    }
}
