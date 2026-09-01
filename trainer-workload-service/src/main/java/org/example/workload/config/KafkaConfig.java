package org.example.workload.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaRetryConfigurationProperties.class)
public class KafkaConfig {
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaRetryConfigurationProperties kafkaRetryConfigurationProperties;

    @Bean
    public DefaultErrorHandler errorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(
                kafkaRetryConfigurationProperties.backoffIntervalMs(), kafkaRetryConfigurationProperties.maxAttempts()));
        errorHandler.addNotRetryableExceptions(InsufficientAuthenticationException.class);
        return errorHandler;
    }
}
