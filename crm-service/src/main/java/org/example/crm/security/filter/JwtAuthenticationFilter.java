package org.example.crm.security.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.crm.security.service.TokenBlackListService;
import org.example.crm.security.service.JwtService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private final static String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final TokenBlackListService tokenBlackListService;

    @Override
    protected @Nullable Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = extractToken(request);
        if(token == null){
            return null;
        }
        if(tokenBlackListService.isBlackListed(token)){
            return null;
        }
        try {
            return jwtService.extractUsername(token);
        }
        catch (JwtException e){
            return null;
        }
    }

    @Override
    protected @Nullable Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return extractToken(request);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
