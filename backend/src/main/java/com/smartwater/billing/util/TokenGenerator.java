package com.smartwater.billing.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure 256-bit random hex token (64 characters).
     */
    public static String generateRawToken() {
        byte[] bytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Computes the SHA-256 hash of a raw token string for secure database storage and lookup.
     */
    public static String hashToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.trim().getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
