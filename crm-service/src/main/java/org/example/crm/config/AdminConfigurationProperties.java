package org.example.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.admin")
public record AdminConfigurationProperties(
        List<String> usernames
) {
}
