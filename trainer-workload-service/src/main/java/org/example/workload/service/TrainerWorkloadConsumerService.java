package org.example.workload.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadConsumerService {
    private final TrainerWorkloadService trainerWorkloadService;

    @PreAuthorize("hasAnyRole('TRAINER', 'TRAINEE', 'ADMIN')")
    @KafkaListener(topics = "${kafka.topics.trainer-workload-update}", groupId = "workload-service-group")
    public void consumeTrainerWorkloadUpdate(@Valid @Payload TrainerWorkloadRequest request) {
        log.info("Started processing workload update for trainer");
        trainerWorkloadService.updateWorkload(request);
        log.info("Finished processing workload update for trainer");

    }
}
