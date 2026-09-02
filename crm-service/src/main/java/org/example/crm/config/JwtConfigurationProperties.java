package org.example.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfigurationProperties(
        String secretKey,
        Long expiration,
        String rolesClaim
) {
}
