package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTopicsConfigurationProperties kafkaTopicsConfigurationProperties;

    @Bean
    public NewTopic trainerWorkloadUpdateTopic(){
        return TopicBuilder.name(kafkaTopicsConfigurationProperties.trainerWorkloadUpdate())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
