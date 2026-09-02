package org.example.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.services")
public record ClientConfigurationProperties(
        String workloadId
) {
}
