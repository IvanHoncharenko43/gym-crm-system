package org.example.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TransactionalMetricService {

    private final MeterRegistry meterRegistry;

    public TransactionalMetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

     public void incrementOnCommit(String metricName, String tagKey, String tagValue) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    meterRegistry.counter(metricName, tagKey, tagValue).increment();
                }
            });
        } else {
            meterRegistry.counter(metricName, tagKey, tagValue).increment();
        }
    }

     public void incrementOnCommit(String metricName) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    meterRegistry.counter(metricName).increment();
                }
            });
        } else {
            meterRegistry.counter(metricName).increment();
        }
    }
}
