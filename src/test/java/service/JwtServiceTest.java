package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.service.JwtService;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-1234567890testsecret");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 10000);

        jwtService.init();
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("test@example.com", "ADMIN");
        assertNotNull(token);
    }

    @Test
    void testExtractEmail() {
        String token = jwtService.generateToken("test@example.com", "USER");

        String email = jwtService.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void testExtractRole() {
        String token = jwtService.generateToken("test@example.com", "ADMIN");

        String role = jwtService.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void testTokenValidity() {
        String token = jwtService.generateToken("test@example.com", "USER");

        assertTrue(jwtService.isTokenValid(token));
    }
}