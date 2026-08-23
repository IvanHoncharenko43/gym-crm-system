package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.services")
public record ClientConfigurationProperties(
        String workloadUrl,
        Long connectTimeout,
        Long readTimeout
) {
}
