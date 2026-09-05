package org.example.workload.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.workload.service.TrainerWorkloadService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadConsumerService {
    private final TrainerWorkloadService trainerWorkloadService;

    @KafkaListener(topics = "${app.kafka.topics.trainer-workload-update}")
    public void consumeTrainerWorkloadUpdate(ConsumerRecord<String, TrainerWorkloadUpdateEvent> record) {
        log.info("Started processing workload update for trainer [topic: {}, partition: {}, offset: {}]",
                record.topic(), record.partition(), record.offset());
        trainerWorkloadService.updateWorkload(record.value());
        log.info("Finished processing workload update for trainer [topic: {}, partition: {}, offset: {}]",
                record.topic(), record.partition(), record.offset());

    }
}
