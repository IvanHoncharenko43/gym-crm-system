package org.example.workload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicsConfigurationProperties(
        String trainerWorkloadUpdate,
        String trainerWorkloadUpdateDlt
) {
}
