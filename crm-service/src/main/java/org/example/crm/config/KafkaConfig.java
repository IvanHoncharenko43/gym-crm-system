package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaAdmin kafkaAdmin;
    private final KafkaTopicsConfigurationProperties kafkaTopicsConfigurationProperties;

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    @Bean
    public NewTopic trainerWorkloadUpdateTopic(){
        return TopicBuilder.name(kafkaTopicsConfigurationProperties.trainerWorkloadUpdate())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
