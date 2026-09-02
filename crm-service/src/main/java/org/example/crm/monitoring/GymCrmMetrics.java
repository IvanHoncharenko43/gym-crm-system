package org.example.crm.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GymCrmMetrics {

    private final MeterRegistry meterRegistry;

    public GymCrmMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementTraineeCreation() {
        meterRegistry.counter("gym_crm_user_creation_requests_total", List.of(Tag.of("user_type", "trainee")))
                .increment();
    }

    public void incrementTrainerCreation() {
        meterRegistry.counter("gym_crm_user_creation_requests_total", List.of(Tag.of("user_type", "trainer")))
                .increment();
    }

    public void incrementTrainerAssignment() {
        meterRegistry.counter("gym_crm_trainer_assignments_total")
                .increment();
    }

    public void incrementTrainingCreated(String trainingType) {
        meterRegistry.counter("gym_crm_trainings_created_total", "training_type", trainingType)
                .increment();
    }
}
