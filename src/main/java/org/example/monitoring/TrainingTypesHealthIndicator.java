package org.example.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypesHealthIndicator(TrainingTypeRepository trainingTypeRepository){
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        try {
            long trainingTypesCount = trainingTypeRepository.count();
            if (trainingTypesCount > 0) {
                return Health.up()
                        .withDetail("message", "Training types are populated")
                        .withDetail("count", trainingTypesCount)
                        .build();
            } else {
                log.warn("Readiness Check Failed: Training Types table is empty");
                return Health.down()
                        .withDetail("reason", "Missing static database constants - Training types")
                        .build();
            }
        } catch (Exception e) {
            log.error("Readiness Check Failed: Database error while checking Training Types", e);
            return Health.down(e)
                    .withDetail("reason", "Database unreachable during check")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
