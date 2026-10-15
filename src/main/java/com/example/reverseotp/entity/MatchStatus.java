package com.example.reverseotp.entity;

public enum MatchStatus {
    APPROVED,
    TOKEN_NOT_FOUND,
    EXPIRED,
    PHONE_MISMATCH,
    ALREADY_APPROVED
}
