package com.transactions.api.gateway.util;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRETKEY", "mysecretkeymysecretkeymysecretkeymysecretkey"); // Set a mock secret key
    }

    @Test
    void testGenerateToken() {
        String userId = "12345";
        String token = jwtUtil.generateToken(userId);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUserId() {
        String userId = "12345";
        String token = jwtUtil.generateToken(userId);

        String extractedUserId = jwtUtil.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken() {
        String userId = "12345";
        String token = jwtUtil.generateToken(userId);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }


    @Test
    void testExtractExpiration() {
        String userId = "12345";
        String token = jwtUtil.generateToken(userId);

        Date expirationDate = ReflectionTestUtils.invokeMethod(jwtUtil, "extractExpiration", token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testExtractAllClaims() {
        String userId = "12345";
        String token = jwtUtil.generateToken(userId);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(jwtUtil, "extractAllClaims", token));
    }

}
