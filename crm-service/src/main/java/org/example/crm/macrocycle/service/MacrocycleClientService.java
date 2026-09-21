package org.example.crm.macrocycle.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.config.ClientConfigurationProperties;
import org.example.crm.exception.DownstreamServiceException;
import org.example.crm.exception.DownstreamUnavailableException;
import org.example.crm.macrocycle.client.MacrocycleClient;
import org.example.crm.macrocycle.client.request.GenerateMacrocycleJobRequest;
import org.example.crm.macrocycle.client.response.MacrocycleJobAcceptedResponse;
import org.example.crm.macrocycle.client.response.MacrocycleJobStatusClientResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MacrocycleClientService {

    private final MacrocycleClient macrocycleClient;
    private final ClientConfigurationProperties clientConfigurationProperties;

    @CircuitBreaker(name = "macrocycleService")
    @Retry(name = "macrocycleService", fallbackMethod = "generateFallback")
    public MacrocycleJobAcceptedResponse generate(GenerateMacrocycleJobRequest request) {
        log.debug("Requesting macrocycle generation for trainee {}", request.traineeUsername());
        MacrocycleJobAcceptedResponse response = macrocycleClient.generate(request);
        log.debug("Macrocycle generation started, job {}", response.jobId());
        return response;
    }

    private MacrocycleJobAcceptedResponse generateFallback(GenerateMacrocycleJobRequest request, Throwable throwable) {
        log.warn("Failed to start macrocycle generation for trainee {} via macrocycle-service", request.traineeUsername());
        return handleFallback(throwable);
    }

    @CircuitBreaker(name = "macrocycleService")
    @Retry(name = "macrocycleService", fallbackMethod = "getStatusFallback")
    public MacrocycleJobStatusClientResponse getStatus(String jobId) {
        log.debug("Fetching macrocycle job status for {}", jobId);
        MacrocycleJobStatusClientResponse response = macrocycleClient.getStatus(jobId);
        log.debug("Retrieved macrocycle job status for {}", jobId);
        return response;
    }

    private MacrocycleJobStatusClientResponse getStatusFallback(String jobId, Throwable throwable) {
        log.warn("Failed to fetch macrocycle job {} status from macrocycle-service", jobId);
        return handleFallback(throwable);
    }

    @CircuitBreaker(name = "macrocycleService")
    @Retry(name = "macrocycleService", fallbackMethod = "discardFallback")
    public void discard(String jobId) {
        log.debug("Discarding macrocycle job {}", jobId);
        macrocycleClient.discard(jobId);
        log.debug("Discarded macrocycle job {}", jobId);
    }

    private void discardFallback(String jobId, Throwable throwable) {
        log.warn("Failed to discard macrocycle job {} via macrocycle-service", jobId);
        handleFallback(throwable);
    }

    private <T> T handleFallback(Throwable throwable) {
        if (throwable instanceof DownstreamServiceException exception) {
            throw exception;
        }
        throw new DownstreamUnavailableException(clientConfigurationProperties.macrocycleId(), throwable);
    }
}
