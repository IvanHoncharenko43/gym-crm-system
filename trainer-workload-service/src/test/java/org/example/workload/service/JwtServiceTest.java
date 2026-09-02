package org.example.workload.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.example.workload.config.JwtConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.example.workload.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_KEY = "SdM6oy/wkfKckcHf2PX1oNTvmC4C7WteH4RLg6Lg6Nw=";
    private static final String OTHER_SECRET_KEY = "Zm9vYmFyYmF6cXV4Zm9vYmFyYmF6cXV4Zm9vYmFyYmF6";
    private static final long EXPIRATION = 7200000L;
    private static final String ROLES_CLAIM = "roles";

    private final JwtConfigurationProperties jwtProperties = new JwtConfigurationProperties(SECRET_KEY, EXPIRATION, ROLES_CLAIM);
    private final JwtService jwtService = new JwtService(jwtProperties);

    @Test
    void extractUsername_ReturnUsername_ValidToken() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), EXPIRATION, SECRET_KEY);

        String username = jwtService.extractUsername(token);
        assertEquals(TRAINER_USERNAME, username);
    }

    @Test
    void extractUsername_ThrowExpiredJwtException_TokenIsExpired() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), -1000L, SECRET_KEY);
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_ThrowSignatureException_TokenSignedWithDifferentKey() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), EXPIRATION, OTHER_SECRET_KEY);
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

    @Test
    void extractAuthorities_ReturnMappedSimpleGrantedAuthorities_ValidTokenWithRolesClaim() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), EXPIRATION, SECRET_KEY);

        List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(token);
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_TRAINER")), authorities);
    }

    @Test
    void isTokenExpired_ReturnFalse_TokenNotExpired() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), EXPIRATION, SECRET_KEY);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_ThrowExpiredJwtException_TokenExpired() {
        String token = buildToken(TRAINER_USERNAME, List.of("ROLE_TRAINER"), -1000L, SECRET_KEY);
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }

    private String buildToken(String subject, List<String> roles, long expirationOffsetMillis, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return Jwts.builder()
                .claims(Map.of(ROLES_CLAIM, roles))
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationOffsetMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
