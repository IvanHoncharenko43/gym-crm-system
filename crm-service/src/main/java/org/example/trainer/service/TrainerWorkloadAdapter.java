package org.example.trainer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainer.controller.TrainerWorkloadClient;
import org.example.trainer.controller.request.TrainerWorkloadRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadAdapter {

    private final TrainerWorkloadClient trainerWorkloadClient;

    @CircuitBreaker(name = "trainerWorkloadService", fallbackMethod = "updateTrainerWorkloadFallback")
    @Retry(name = "trainerWorkloadService")
    public void updateTrainerWorkload(TrainerWorkloadRequest request){
        log.debug("Sending an update on trainer workload");
        trainerWorkloadClient.updateTrainerWorkload(request);
        log.debug("Updated a trainer's workload");
    }

    private void updateTrainerWorkloadFallback(TrainerWorkloadRequest request, Throwable throwable){
        String transactionId = MDC.get("traceId");
        log.error("[TxID: {}] Circuit breaker tripped for Workload Service. Reason: {}",
                transactionId, throwable.getMessage());
        log.warn("[TxID: {}] Workload out of sync. Proceeding without external update. Payload: {}",
                transactionId, request);
    }
}
