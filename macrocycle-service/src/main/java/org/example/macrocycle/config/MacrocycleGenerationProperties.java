package org.example.macrocycle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "macrocycle")
public record MacrocycleGenerationProperties(
        Generation generation,
        int jobRetentionDays
) {
    public record Generation(
            int maxIterations,
            Duration plannerTimeout,
            Duration verifierTimeout,
            Duration jobTimeout,
            int corePoolSize,
            int maxPoolSize
    ) {
    }
}
