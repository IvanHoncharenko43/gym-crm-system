package org.example.crm.trainer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadSendCallback implements BiConsumer<SendResult<String, TrainerWorkloadUpdateEvent>, Throwable> {

    private final Map<String, String> mdcContext;

    @Override
    public void accept(SendResult<String, TrainerWorkloadUpdateEvent> result, Throwable throwable) {
        if(mdcContext != null){
            MDC.setContextMap(mdcContext);
        }
        try {
            if (throwable == null) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("Successfully sent workload update for trainer [topic: {}, partition: {}, offset: {}]",
                        recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            } else {
                log.error("Failed to send workload update for trainer", throwable);
            }
        } finally {
            MDC.clear();
        }
    }
}
