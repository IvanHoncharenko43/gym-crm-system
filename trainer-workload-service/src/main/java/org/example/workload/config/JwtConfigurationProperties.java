package org.example.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtConfigurationProperties(
        String secretKey,
        Long expiration,
        String rolesClaim
) {
}
