package org.example.crm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerLoggingConfig {

    private final CircuitBreakerRegistry registry;

    @PostConstruct
    public void registerCircuitBreakerLogging() {
        registry.circuitBreaker("trainerWorkloadService")
                .getEventPublisher()
                .onStateTransition(event ->
                    log.warn("Circuit Breaker, trainerWorkloadService, changed state from {} to {}",
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()))
                .onError(event ->
                    log.error("Circuit Breaker, trainerWorkloadService, recorded a failure: {}",
                            event.getThrowable().getMessage()));
    }
}
