package org.example.crm.trainer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.config.ClientConfigurationProperties;
import org.example.crm.core.service.GymMapper;
import org.example.crm.exception.DownstreamServiceException;
import org.example.crm.exception.DownstreamUnavailableException;
import org.example.crm.trainer.client.TrainerWorkloadClient;
import org.example.crm.trainer.controller.request.TrainerMonthlyWorkloadRequest;
import org.example.crm.trainer.client.response.TrainerWorkloadClientResponse;
import org.example.crm.trainer.client.request.TrainerMonthlyWorkloadClientRequest;
import org.example.crm.trainer.controller.response.TrainerWorkloadSummary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadClient trainerWorkloadClient;
    private final GymMapper gymMapper;
    private final ClientConfigurationProperties clientConfigurationProperties;

    @CircuitBreaker(name = "trainerWorkloadService", fallbackMethod = "getWorkloadFallback")
    @Retry(name = "trainerWorkloadService")
    public TrainerWorkloadSummary getWorkload(TrainerMonthlyWorkloadRequest request) {
        log.debug("Requesting trainer workload summary for {}/{}", request.month(), request.year());
        TrainerMonthlyWorkloadClientRequest clientRequest = gymMapper.toTrainerMonthlyWorkloadClientRequest(request);
        TrainerWorkloadClientResponse response = trainerWorkloadClient.getWorkload(clientRequest);
        log.debug("Retrieved trainer workload summary");
        return gymMapper.toTrainerWorkloadSummary(response);
    }

    private TrainerWorkloadSummary getWorkloadFallback(TrainerMonthlyWorkloadClientRequest query, Throwable throwable) {
        log.warn("Failed to retrieve trainer workload summary for {}/{} from trainer-workload-service",
                query.month(), query.year());
        if (throwable instanceof DownstreamServiceException exception){
            throw exception;
        }
        throw new DownstreamUnavailableException(clientConfigurationProperties.workloadId(), throwable);
    }
}
