package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.example.TestUtils;
import org.example.config.JwtConfigurationProperties;
import org.example.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private static final String SECRET_KEY = "SdM6oy/wkfKckcHf2PX1oNTvmC4C7WteH4RLg6Lg6Nw=";
    private static final String OTHER_SECRET_KEY = "Zm9vYmFyYmF6cXV4Zm9vYmFyYmF6cXV4Zm9vYmFyYmF6";
    private static final long EXPIRATION = 7200000L;
    private static final String ROLES_CLAIM = "roles";

    private final JwtConfigurationProperties jwtProperties = new JwtConfigurationProperties(SECRET_KEY, EXPIRATION, ROLES_CLAIM);
    private final JwtService jwtService = new JwtService(jwtProperties);

    @Test
    void generateToken_EmbedUsernameAsSubjectAndRolesClaim_ValidUserDetails() {
        UserDetails userDetails = TestUtils.getTraineeUserDetails();

        String token = jwtService.generateToken(userDetails);
        Claims claims = parseClaims(token, SECRET_KEY);

        assertEquals(TestUtils.TRAINEE_USERNAME, claims.getSubject());
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        assertTrue(roles.contains("ROLE_TRAINEE"));
    }

    @Test
    void generateToken_UseCustomExtraClaims_MapAndValidUserDetails() {
        UserDetails userDetails = TestUtils.getTraineeUserDetails();
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("custom", "value");

        String token = jwtService.generateToken(extraClaims, userDetails);
        Claims claims = parseClaims(token, SECRET_KEY);

        assertEquals(TestUtils.TRAINEE_USERNAME, claims.getSubject());
        assertEquals("value", claims.get("custom"));
        assertNull(claims.get(ROLES_CLAIM));
    }

    @Test
    void extractUsername_ReturnUsername_ValidToken() {
        String token = jwtService.generateToken(TestUtils.getTraineeUserDetails());

        String username = jwtService.extractUsername(token);
        assertEquals(TestUtils.TRAINEE_USERNAME, username);
    }

    @Test
    void extractUsername_ThrowExpiredJwtException_TokenIsExpired() {
        JwtConfigurationProperties expiredProperties = new JwtConfigurationProperties(SECRET_KEY, -100L, ROLES_CLAIM);
        JwtService expiredTokenService = new JwtService(expiredProperties);
        String token = expiredTokenService.generateToken(TestUtils.getTraineeUserDetails());

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_ThrowSignatureException_TokenSignedWithDifferentKey() {
        JwtConfigurationProperties otherProperties = new JwtConfigurationProperties(OTHER_SECRET_KEY, EXPIRATION, ROLES_CLAIM);
        JwtService otherKeyService = new JwtService(otherProperties);
        String token = otherKeyService.generateToken(TestUtils.getTraineeUserDetails());

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_ThrowMalformedJwtException_TokenIsMalformed() {
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername("not-a-jwt"));
    }

    @Test
    void extractUsername_ThrowIllegalArgumentException_TokenIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(null));
        assertThrows(IllegalArgumentException.class, () -> jwtService.extractUsername(""));
    }

    private Claims parseClaims(String token, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
