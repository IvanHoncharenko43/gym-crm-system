package org.example.crm.trainer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.crm.config.KafkaTopicsConfigurationProperties;
import org.example.crm.config.RequestHeaderContextResolver;
import org.example.crm.trainer.client.request.TrainerUpdateWorkloadClientRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaTopicsConfigurationProperties.class)
public class TrainerWorkloadProducerService {
    private final KafkaTemplate<String, TrainerUpdateWorkloadClientRequest> kafkaTemplate;
    private final RequestHeaderContextResolver requestHeaderContextResolver;
    private final KafkaTopicsConfigurationProperties kafkaTopicsConfigurationProperties;

    public void publishTrainerWorkloadUpdateEvent(TrainerUpdateWorkloadClientRequest request){
        log.debug("Publishing workload update for trainer");
        String messageKey = request.username();
        ProducerRecord<String, TrainerUpdateWorkloadClientRequest> record = new ProducerRecord<>(
                kafkaTopicsConfigurationProperties.trainerWorkloadUpdate(), messageKey, request);

        requestHeaderContextResolver.getAuthorizationHeader()
                .ifPresent(authorizationHeader -> record.headers().add(AUTHORIZATION, authorizationHeader.getBytes(StandardCharsets.UTF_8)));
        requestHeaderContextResolver.getTraceIdHeader()
                .ifPresent(traceIdHeader -> record.headers().add(TRACE_ID_KEY, traceIdHeader.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent workload update for trainer");
                    } else {
                        log.error("Failed to send workload update for trainer", ex);
                    }
                });
    }
}
