package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "auth")
public record AuthConfigurationProperties(
        Set<String> bypassPostPaths
) {
}
