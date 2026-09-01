package org.example.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.consumer.retry")
public record KafkaRetryConfigurationProperties(
        long backoffIntervalMs,
        int maxAttempts
) {
}
