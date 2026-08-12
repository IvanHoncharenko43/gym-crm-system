package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "interceptor")
public record InterceptorConfigurationProperties(
        String includePath
) {
}
