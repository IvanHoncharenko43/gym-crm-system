package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security.admin")
public record AdminConfigurationProperties(
        List<String> usernames
) {
}
