package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record ClientConfigurationProperties(
        String workloadUrl
) {
}
