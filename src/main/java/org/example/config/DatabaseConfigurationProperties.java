package org.example.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record DatabaseConfigurationProperties(
        @Value("${db.driver}") String driver,
        @Value("${db.url}") String url,
        @Value("${db.username}") String username,
        @Value("${db.password}") String password
) {
}
