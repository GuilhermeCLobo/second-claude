package com.marketplace.backend.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "unit-test-marketplace-jwt-signing-secret-key-value", 60);

    @Test
    void generatedTokenCanBeParsedBackToTheSameUserId() {
        String token = jwtService.generateToken(42L, "alice");

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }
}
