package com.example.reverseotp.service;

import com.example.reverseotp.dto.request.GenerateTokenRequest;
import com.example.reverseotp.dto.response.GenerateTokenResponse;
import com.example.reverseotp.dto.response.TokenStatusResponse;
import com.example.reverseotp.entity.TokenRequest;
import com.example.reverseotp.entity.TokenStatus;
import com.example.reverseotp.exception.ResourceNotFoundException;
import com.example.reverseotp.repository.TokenRequestRepository;
import com.example.reverseotp.util.PhoneNumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final String HEX_CHARS = "0123456789ABCDEF";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenRequestRepository tokenRequestRepository;

    @Value("${app.token.ttl-minutes:5}")
    private long ttlMinutes;

    @Value("${app.token.ttl-seconds:0}")
    private long ttlSeconds;

    @Value("${app.token.request-id-length:16}")
    private int requestIdLength;

    @Value("${app.token.suffix-length:48}")
    private int suffixLength;

    @Value("${app.token.sms-template:%s}")
    private String smsTemplate;

    public TokenService(TokenRequestRepository tokenRequestRepository) {
        this.tokenRequestRepository = tokenRequestRepository;
    }

    private Duration getTokenTtl() {
        if (ttlSeconds > 0) {
            return Duration.ofSeconds(ttlSeconds);
        }
        return Duration.ofMinutes(ttlMinutes);
    }

    /**
     * Generates a unique 16-char hex prefixed token (64 hex chars total, < 160 chars) and persists it in token_requests.
     */
    @Transactional
    public GenerateTokenResponse generateToken(GenerateTokenRequest request) {
        String normalizedPhone = PhoneNumberUtil.normalize(request.getPhoneNumber());

        // Generate 16-character uppercase hex Request ID
        String requestId = generateRandomHex(requestIdLength);

        // Generate hex random suffix (default 48 hex chars)
        String hexSuffix = generateRandomHex(suffixLength);

        // Full token is pure hex where first 16 characters are requestId
        String fullToken = requestId + hexSuffix;

        Instant now = Instant.now();
        Instant expiresAt = now.plus(getTokenTtl());

        TokenRequest tokenRequest = new TokenRequest(
                requestId,
                normalizedPhone,
                fullToken,
                TokenStatus.PENDING,
                expiresAt,
                now
        );

        tokenRequestRepository.save(tokenRequest);
        log.info("Generated token request: requestId={}, phone={}, length={}, expiresAt={}",
                requestId, normalizedPhone, fullToken.length(), expiresAt);

        String smsContent = smsTemplate.contains("%s")
                ? String.format(smsTemplate, fullToken)
                : smsTemplate + fullToken;

        return new GenerateTokenResponse(requestId, fullToken, normalizedPhone, smsContent, expiresAt);
    }

    /**
     * Retrieves token verification status and phone number by request ID.
     */
    @Transactional
    public TokenStatusResponse getTokenStatus(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new ResourceNotFoundException("Request ID must not be blank");
        }

        String sanitizedId = requestId.trim().toUpperCase();

        TokenRequest tokenRequest = tokenRequestRepository.findByRequestId(sanitizedId)
                .orElseThrow(() -> new ResourceNotFoundException("Token request not found with ID: " + sanitizedId));

        // Lazily expire if time has passed and still PENDING
        if (tokenRequest.getStatus() == TokenStatus.PENDING && Instant.now().isAfter(tokenRequest.getExpiresAt())) {
            tokenRequest.setStatus(TokenStatus.EXPIRED);
            tokenRequestRepository.save(tokenRequest);
            log.info("Token request {} marked as EXPIRED during status check", sanitizedId);
        }

        log.info("Token status checked: requestId={}, status={}", sanitizedId, tokenRequest.getStatus());

        return new TokenStatusResponse(
                tokenRequest.getRequestId(),
                tokenRequest.getPhoneNumber(),
                tokenRequest.getStatus(),
                tokenRequest.getCreatedAt(),
                tokenRequest.getExpiresAt(),
                tokenRequest.getVerifiedAt()
        );
    }

    private String generateRandomHex(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(HEX_CHARS.charAt(RANDOM.nextInt(HEX_CHARS.length())));
        }
        return sb.toString();
    }
}
