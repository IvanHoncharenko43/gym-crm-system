package org.example.crm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerLoggingConfig {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> customCircuitBreakerRegistryEventConsumer(){
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry()
                        .getEventPublisher()
                        .onStateTransition(event ->
                                log.warn("Circuit Breaker '{}' changed state from {} to {}",
                                        event.getCircuitBreakerName(),
                                        event.getStateTransition().getFromState(),
                                        event.getStateTransition().getToState()))
                        .onError(event ->
                                log.error("Circuit Breaker '{}' recorded a failure: {}",
                                        event.getCircuitBreakerName(),
                                        event.getThrowable().getMessage()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {

            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {

            }
        };
    }
}
