package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record ServerConfigurationProperties(
        @Value("${server.servlet.mapping}") String servletMapping,
        @Value("${server.api.mapping}") String apiMapping
){
}
