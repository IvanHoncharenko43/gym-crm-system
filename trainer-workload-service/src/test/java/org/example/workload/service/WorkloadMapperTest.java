package org.example.workload.service;

import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.junit.jupiter.api.Test;

import java.time.Month;

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
    void toTrainerWorkloadDocument_MapCorrectly_FromRequest() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadMapper.toTrainerWorkloadDocument(request, null);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(request.username(), result.getUsername());
        assertEquals(request.fullName().firstName(), result.getFirstName());
        assertEquals(request.fullName().lastName(), result.getLastName());
        assertEquals(request.isActive(), result.isStatus());
        assertEquals(request.trainingDate().getYear(), result.getYear());
        assertTrue(result.getMonths().isEmpty());
    }

    @Test
    void toTrainerWorkloadDocument_MapCorrectly_FromRequestAndTrainerWorkloadEntity() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        String id = "21";
        existingWorkload.setId(id);
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(Month.MAY, 0);
        existingWorkload.getMonths().add(monthWorkload);

        TrainerWorkloadEntity result = workloadMapper.toTrainerWorkloadDocument(request, existingWorkload);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(request.username(), result.getUsername());
        assertEquals(request.fullName().firstName(), result.getFirstName());
        assertEquals(request.fullName().lastName(), result.getLastName());
        assertEquals(request.isActive(), result.isStatus());
        assertEquals(request.trainingDate().getYear(), result.getYear());
        assertEquals(existingWorkload.getMonths(), result.getMonths());
    }

    @Test
    void toMonthWorkload_MapCorrectly_FromMonthValueAndYearWorkloadEntity() {
        MonthWorkloadEntity result = workloadMapper.toMonthWorkload(TRAINING_DATE.getMonth());

        assertNotNull(result);
        assertEquals(TRAINING_DATE.getMonth(), result.getMonth());
        assertEquals(0, result.getTrainingSummaryDurationMinutes());
    }
}
