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
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenRequestRepository tokenRequestRepository;

    @Value("${app.token.ttl-minutes:5}")
    private long ttlMinutes;

    public TokenService(TokenRequestRepository tokenRequestRepository) {
        this.tokenRequestRepository = tokenRequestRepository;
    }

    /**
     * Generates a unique 16-char prefixed token (< 160 chars) and persists it in token_requests.
     */
    @Transactional
    public GenerateTokenResponse generateToken(GenerateTokenRequest request) {
        String normalizedPhone = PhoneNumberUtil.normalize(request.getPhoneNumber());

        // Generate a 16-character uppercase alphanumeric Request ID
        String requestId = generateRandomString(16);

        // Generate 6-digit random verification suffix
        String suffix = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Full token starts with first 16 characters as requestId
        String fullToken = requestId + "-" + suffix;

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(ttlMinutes));

        TokenRequest tokenRequest = new TokenRequest(
                requestId,
                normalizedPhone,
                fullToken,
                TokenStatus.PENDING,
                expiresAt,
                now
        );

        tokenRequestRepository.save(tokenRequest);
        log.info("Generated token request: requestId={}, phone={}, expiresAt={}", requestId, normalizedPhone, expiresAt);

        String smsContent = "Your verification token is: " + fullToken;

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

        return new TokenStatusResponse(
                tokenRequest.getRequestId(),
                tokenRequest.getPhoneNumber(),
                tokenRequest.getStatus(),
                tokenRequest.getCreatedAt(),
                tokenRequest.getExpiresAt(),
                tokenRequest.getVerifiedAt()
        );
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
