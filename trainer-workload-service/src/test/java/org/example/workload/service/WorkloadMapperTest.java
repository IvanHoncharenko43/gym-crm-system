package org.example.workload.service;

import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.junit.jupiter.api.Test;

import static org.example.workload.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkloadMapperTest {

    private final WorkloadMapper workloadMapper = new WorkloadMapper();

    @Test
    void toTrainerWorkloadSummary_MapCorrectly_FromWorkloadEntityAndYearAndMonthAndDuration() {
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        TrainerWorkloadSummary result = workloadMapper.toTrainerWorkloadSummary(
                trainerWorkload, TRAINING_DATE.getYear(), TRAINING_DATE.getMonthValue(), DURATION_MINUTES
        );
        assertNotNull(result);
        assertEquals(trainerWorkload.getUsername(), result.username());
        assertEquals(trainerWorkload.getFirstName(), result.fullName().firstName());
        assertEquals(trainerWorkload.getLastName(), result.fullName().lastName());
        assertEquals(trainerWorkload.isStatus(), result.isActive());
        assertEquals(TRAINING_DATE.getYear(), result.year());
        assertEquals(TRAINING_DATE.getMonthValue(), result.month());
        assertEquals(DURATION_MINUTES, result.trainingSummaryDurationMinutes());
    }
}
