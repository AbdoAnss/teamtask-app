package com.teamflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "test_secret_key_for_unit_tests_only_256bits_padded_000000";
    private static final long EXPIRATION_MS = 3600000L;

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void init() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    void generatedTokenShouldValidateAndCarryUsername() {
        String token = tokenProvider.generateToken("alice");

        assertTrue(tokenProvider.validateToken(token));
        assertEquals("alice", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void garbageTokenShouldNotValidate() {
        assertFalse(tokenProvider.validateToken("not-a-jwt"));
    }

    @Test
    void tokenSignedWithDifferentKeyShouldNotValidate() {
        JwtTokenProvider other = new JwtTokenProvider(
            "another_secret_key_padded_to_reach_256_bits_total_0000000000", EXPIRATION_MS);

        String foreignToken = other.generateToken("mallory");

        assertFalse(tokenProvider.validateToken(foreignToken));
    }

    @Test
    void expiredTokenShouldNotValidate() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, -1000L);
        String expired = shortLived.generateToken("alice");

        assertFalse(tokenProvider.validateToken(expired));
    }

    @Test
    void getExpirationMsShouldReturnConfiguredValue() {
        assertEquals(EXPIRATION_MS, tokenProvider.getExpirationMs());
    }
}
