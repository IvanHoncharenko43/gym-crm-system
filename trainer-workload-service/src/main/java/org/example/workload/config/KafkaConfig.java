package org.example.workload.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KafkaRetryConfigurationProperties.class, KafkaTopicsConfigurationProperties.class})
public class KafkaConfig {

    private final KafkaAdmin kafkaAdmin;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaRetryConfigurationProperties kafkaRetryConfigurationProperties;
    private final KafkaTopicsConfigurationProperties kafkaTopicsConfigurationProperties;

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(
                        kafkaTopicsConfigurationProperties.trainerWorkloadUpdateDlt(), record.partition()));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(
                kafkaRetryConfigurationProperties.backoffIntervalMs(), kafkaRetryConfigurationProperties.maxAttempts()));
        return errorHandler;
    }

    @Bean
    public NewTopic trainerWorkloadUpdateTopic(){
        return TopicBuilder.name(kafkaTopicsConfigurationProperties.trainerWorkloadUpdate())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic trainerWorkloadUpdateDltTopic(){
        return TopicBuilder.name(kafkaTopicsConfigurationProperties.trainerWorkloadUpdateDlt())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
