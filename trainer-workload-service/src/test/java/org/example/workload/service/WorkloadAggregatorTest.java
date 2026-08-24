package org.example.workload.service;

import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.exception.InvalidStateTransitionException;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.YearWorkloadEntity;
import org.junit.jupiter.api.Test;

import java.time.Month;

import static org.example.workload.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class WorkloadAggregatorTest {

    private final WorkloadAggregator workloadAggregator = new WorkloadAggregator();

    @Test
    void apply_CreateNewEntityWithYearAndMonth_ExistingIsNullAndActionIsAdd() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadAggregator.apply(null, request);

        assertNotNull(result);
        assertEquals(TRAINER_USERNAME, result.getUsername());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertTrue(result.isStatus());
        assertEquals(1, result.getYears().size());
        YearWorkloadEntity year = result.getYears().getFirst();
        assertEquals(TRAINING_DATE.getYear(), year.getYear());
        assertEquals(1, year.getMonths().size());
        MonthWorkloadEntity month = year.getMonths().getFirst();
        assertEquals(TRAINING_DATE.getMonth(), month.getMonth());
        assertEquals(DURATION_MINUTES, month.getTrainingSummaryDurationMinutes());
    }

    @Test
    void apply_ThrowWorkloadNotFoundException_ExistingIsNullAndActionIsDelete() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        assertThrows(WorkloadNotFoundException.class, () -> workloadAggregator.apply(null, request));
    }

    @Test
    void apply_CreateNewYear_ExistingEntityHasNoMatchingYear() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity otherYear = getYearWorkloadEntity(TRAINING_DATE.getYear() - 1);
        existingWorkload.getYears().add(otherYear);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadAggregator.apply(existingWorkload, request);
        assertEquals(2, result.getYears().size());
        YearWorkloadEntity newYear = result.getYears().stream()
                .filter(year -> year.getYear() == TRAINING_DATE.getYear())
                .findFirst()
                .orElseThrow();
        assertEquals(1, newYear.getMonths().size());
        assertEquals(DURATION_MINUTES, newYear.getMonths().getFirst().getTrainingSummaryDurationMinutes());
    }

    @Test
    void apply_CreateNewMonthInExistingYear_YearExistsButMonthMissing() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        year.getMonths().add(getMonthWorkloadEntity(Month.JANUARY, 30));
        existingWorkload.getYears().add(year);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(TRAINING_DATE, ActionType.ADD, DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadAggregator.apply(existingWorkload, request);

        assertEquals(1, result.getYears().size());
        assertEquals(2, result.getYears().getFirst().getMonths().size());
        MonthWorkloadEntity newMonth = result.getYears().getFirst().getMonths().stream()
                .filter(m -> m.getMonth() == TRAINING_DATE.getMonth())
                .findFirst()
                .orElseThrow();
        assertEquals(DURATION_MINUTES, newMonth.getTrainingSummaryDurationMinutes());
    }

    @Test
    void apply_AddDurationToExistingMonth_ActionIsAdd() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity month = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 60);
        year.getMonths().add(month);
        existingWorkload.getYears().add(year);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(TRAINING_DATE, ActionType.ADD, 30);

        TrainerWorkloadEntity result = workloadAggregator.apply(existingWorkload, request);
        assertSame(existingWorkload, result);
        assertEquals(90, month.getTrainingSummaryDurationMinutes());
    }

    @Test
    void apply_SubtractDurationFromExistingMonth_ActionIsDelete() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity month = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 90);
        year.getMonths().add(month);
        existingWorkload.getYears().add(year);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(TRAINING_DATE, ActionType.DELETE, 30);

        workloadAggregator.apply(existingWorkload, request);
        assertEquals(60, month.getTrainingSummaryDurationMinutes());
    }

    @Test
    void apply_ThrowWorkloadNotFoundException_ActionIsDeleteAndMonthMissing() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        year.getMonths().add(getMonthWorkloadEntity(Month.JANUARY, 30));
        existingWorkload.getYears().add(year);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(TRAINING_DATE, ActionType.DELETE, 30);

        assertThrows(WorkloadNotFoundException.class, () -> workloadAggregator.apply(existingWorkload, request));
    }

    @Test
    void apply_ThrowInvalidStateTransitionException_ResultingDurationIsNegative() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity month = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 20);
        year.getMonths().add(month);
        existingWorkload.getYears().add(year);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(TRAINING_DATE, ActionType.DELETE, 30);

        assertThrows(InvalidStateTransitionException.class, () -> workloadAggregator.apply(existingWorkload, request));
    }

    @Test
    void apply_UpdateFirstNameAndLastNameAndStatus_ExistingEntityFieldsChangeOnEveryCall() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, "Old", "Name", false);
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);

        TrainerWorkloadEntity result = workloadAggregator.apply(existingWorkload, request);
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertTrue(result.isStatus());
    }
}
