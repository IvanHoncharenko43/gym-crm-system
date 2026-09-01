package org.example.workload.service;

import org.example.workload.controller.dto.ActionType;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.YearWorkloadEntity;
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

    @Test
    void toTrainerWorkloadEntity_MapCorrectly_FromRequest() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadMapper.toTrainerWorkloadEntity(request, null);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(request.username(), result.getUsername());
        assertEquals(request.fullName().firstName(), result.getFirstName());
        assertEquals(request.fullName().lastName(), result.getLastName());
        assertEquals(request.isActive(), result.isStatus());
        assertTrue(result.getYears().isEmpty());
    }

    @Test
    void toTrainerWorkloadEntity_MapCorrectly_FromRequestAndTrainerWorkloadEntity() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        existingWorkload.setId(21L);
        YearWorkloadEntity existingYear = getYearWorkloadEntity(TRAINING_DATE.getYear());
        existingWorkload.getYears().add(existingYear);

        TrainerWorkloadEntity result = workloadMapper.toTrainerWorkloadEntity(request, existingWorkload);

        assertNotNull(result);
        assertEquals(21L, result.getId());
        assertEquals(request.username(), result.getUsername());
        assertEquals(request.fullName().firstName(), result.getFirstName());
        assertEquals(request.fullName().lastName(), result.getLastName());
        assertEquals(request.isActive(), result.isStatus());
        assertEquals(existingWorkload.getYears(), result.getYears());
    }

    @Test
    void toYearWorkloadEntity_MapCorrectly_FromYearValueAndTrainerWorkloadEntity() {
        YearWorkloadEntity result = workloadMapper.toYearWorkloadEntity(TRAINING_DATE.getYear());

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(TRAINING_DATE.getYear(), result.getYear());
        assertTrue(result.getMonths().isEmpty());
    }

    @Test
    void toMonthWorkloadEntity_MapCorrectly_FromMonthValueAndYearWorkloadEntity() {
        MonthWorkloadEntity result = workloadMapper.toMonthWorkloadEntity(TRAINING_DATE.getMonth());

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(TRAINING_DATE.getMonth(), result.getMonth());
        assertEquals(0, result.getTrainingSummaryDurationMinutes());
    }
}
