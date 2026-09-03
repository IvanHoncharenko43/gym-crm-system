package org.example.crm.trainer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.crm.config.KafkaTopicsConfigurationProperties;
import org.example.crm.config.RequestHeaderContextResolver;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaTopicsConfigurationProperties.class)
public class TrainerWorkloadProducerService {

    private final KafkaTemplate<String, TrainerWorkloadUpdateEvent> kafkaTemplate;
    private final RequestHeaderContextResolver requestHeaderContextResolver;
    private final KafkaTopicsConfigurationProperties kafkaTopicsConfigurationProperties;

    public void publishTrainerWorkloadUpdateEvent(TrainerWorkloadUpdateEvent event){
        log.debug("Publishing workload update for trainer");
        String messageKey = event.username();
        ProducerRecord<String, TrainerWorkloadUpdateEvent> record = new ProducerRecord<>(
                kafkaTopicsConfigurationProperties.trainerWorkloadUpdate(), messageKey, event);

        requestHeaderContextResolver.getTraceIdHeader()
                .ifPresent(traceIdHeader -> record.headers().add(TRACE_ID_KEY, traceIdHeader.getBytes(StandardCharsets.UTF_8)));
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if(mdcContext != null){
                        MDC.setContextMap(mdcContext);
                    }
                    try {
                        if (ex == null) {
                            log.info("Successfully sent workload update for trainer");
                        } else {
                            log.error("Failed to send workload update for trainer", ex);
                        }
                    } finally {
                        MDC.clear();
                    }
                });
    }
}
