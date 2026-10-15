package com.example.reverseotp.dto.response;

import com.example.reverseotp.entity.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned to telco webhook caller after processing SMS")
public class WebhookProcessResponse {

    @Schema(description = "ID assigned to incoming message in database", example = "1")
    private Long incomingMessageId;

    @Schema(description = "Extracted 16-character request ID", example = "A1B2C3D4E5F67890")
    private String extractedRequestId;

    @Schema(description = "Whether the token matched a pending request", example = "true")
    private boolean matched;

    @Schema(description = "Detailed match outcome", example = "APPROVED")
    private MatchStatus matchStatus;

    @Schema(description = "Human-readable description of outcome", example = "Token verified and approved successfully")
    private String message;

    public WebhookProcessResponse() {
    }

    public WebhookProcessResponse(Long incomingMessageId, String extractedRequestId, boolean matched,
                                  MatchStatus matchStatus, String message) {
        this.incomingMessageId = incomingMessageId;
        this.extractedRequestId = extractedRequestId;
        this.matched = matched;
        this.matchStatus = matchStatus;
        this.message = message;
    }

    public Long getIncomingMessageId() {
        return incomingMessageId;
    }

    public void setIncomingMessageId(Long incomingMessageId) {
        this.incomingMessageId = incomingMessageId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
