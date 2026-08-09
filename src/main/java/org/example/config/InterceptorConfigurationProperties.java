package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record InterceptorConfigurationProperties(
        @Value("${interceptor.path.include}") String includePath
) {
}
