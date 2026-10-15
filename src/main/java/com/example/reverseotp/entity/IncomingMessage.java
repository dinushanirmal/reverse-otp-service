package com.example.reverseotp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "incoming_messages")
public class IncomingMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_number", length = 30, nullable = false)
    private String senderNumber;

    @Column(name = "raw_content", length = 500, nullable = false)
    private String rawContent;

    @Column(name = "extracted_request_id", length = 16)
    private String extractedRequestId;

    @Column(name = "matched", nullable = false)
    private boolean matched;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", length = 30, nullable = false)
    private MatchStatus matchStatus;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    public IncomingMessage() {
    }

    public IncomingMessage(String senderNumber, String rawContent, String extractedRequestId,
                           boolean matched, MatchStatus matchStatus, Instant receivedAt) {
        this.senderNumber = senderNumber;
        this.rawContent = rawContent;
        this.extractedRequestId = extractedRequestId;
        this.matched = matched;
        this.matchStatus = matchStatus;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getExtractedRequestId() {
        return extractedRequestId;
    }

    public void setExtractedRequestId(String extractedRequestId) {
        this.extractedRequestId = extractedRequestId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
