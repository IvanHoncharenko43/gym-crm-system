package org.example.workload.service;

import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.exception.InvalidStateTransitionException;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.example.workload.repository.YearWorkloadEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Month;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.example.workload.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private WorkloadAggregator workloadAggregator;

    @Mock
    private WorkloadMapper workloadMapper;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateWorkload_ComputeAndSaveWorkload_ExistingWorkloadFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        TrainerWorkloadEntity updatedWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        when(workloadAggregator.apply(existingWorkload, request)).thenReturn(updatedWorkload);
        when(trainerWorkloadRepository.computeAndSave(eq(TRAINER_USERNAME), any())).thenAnswer(invocation -> {
            UnaryOperator<TrainerWorkloadEntity> updater = invocation.getArgument(1);
            return updater.apply(existingWorkload);
        });

        trainerWorkloadService.updateWorkload(request);
        verify(workloadAggregator, times(1)).apply(existingWorkload, request);
        verify(trainerWorkloadRepository, times(1)).computeAndSave(eq(TRAINER_USERNAME), any());
    }

    @Test
    void updateWorkload_ComputeAndSaveWorkload_ExistingWorkloadNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity createdWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        when(workloadAggregator.apply(null, request)).thenReturn(createdWorkload);
        when(trainerWorkloadRepository.computeAndSave(eq(TRAINER_USERNAME), any())).thenAnswer(invocation -> {
            UnaryOperator<TrainerWorkloadEntity> updater = invocation.getArgument(1);
            return updater.apply(null);
        });

        trainerWorkloadService.updateWorkload(request);
        verify(workloadAggregator, times(1)).apply(null, request);
        verify(trainerWorkloadRepository, times(1)).computeAndSave(eq(TRAINER_USERNAME), any());
    }

    @Test
    void updateWorkload_ThrowException_AggregatorThrowsWorkloadNotFoundException() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);

        when(workloadAggregator.apply(null, request)).thenThrow(new WorkloadNotFoundException("Workload not found"));
        when(trainerWorkloadRepository.computeAndSave(eq(TRAINER_USERNAME), any())).thenAnswer(invocation -> {
            UnaryOperator<TrainerWorkloadEntity> updater = invocation.getArgument(1);
            return updater.apply(null);
        });

        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.updateWorkload(request));
    }

    @Test
    void updateWorkload_ThrowException_AggregatorThrowsInvalidStateTransitionException() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        when(workloadAggregator.apply(existingWorkload, request)).thenThrow(new InvalidStateTransitionException("Cannot go negative"));
        when(trainerWorkloadRepository.computeAndSave(eq(TRAINER_USERNAME), any())).thenAnswer(invocation -> {
            UnaryOperator<TrainerWorkloadEntity> updater = invocation.getArgument(1);
            return updater.apply(existingWorkload);
        });

        assertThrows(InvalidStateTransitionException.class, () -> trainerWorkloadService.updateWorkload(request));
    }

    @Test
    void getMonthlySummary_ReturnSummaryWithMatchingDuration_YearAndMonthFound() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadEntity workload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity month = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        year.getMonths().add(month);
        workload.getYears().add(year);
        TrainerWorkloadSummary expected = getTrainerWorkloadSummary(query.year(), query.month(), DURATION_MINUTES);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(workload));
        when(workloadMapper.toTrainerWorkloadSummary(workload, query.year(), query.month(), DURATION_MINUTES)).thenReturn(expected);

        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlySummary(query);
        assertEquals(expected, result);
        verify(workloadMapper, times(1)).toTrainerWorkloadSummary(workload, query.year(), query.month(), DURATION_MINUTES);
    }

    @Test
    void getMonthlySummary_ReturnSummaryWithZeroDuration_YearNotFound() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadEntity workload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        workload.getYears().add(getYearWorkloadEntity(query.year() - 1));
        TrainerWorkloadSummary expected = getTrainerWorkloadSummary(query.year(), query.month(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(workload));
        when(workloadMapper.toTrainerWorkloadSummary(workload, query.year(), query.month(), 0)).thenReturn(expected);

        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlySummary(query);
        assertEquals(expected, result);
        verify(workloadMapper, times(1)).toTrainerWorkloadSummary(workload, query.year(), query.month(), 0);
    }

    @Test
    void getMonthlySummary_ReturnSummaryWithZeroDuration_MonthNotFoundInYear() {
        WorkloadQuery query = getWorkloadQuery();
        TrainerWorkloadEntity workload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity year = getYearWorkloadEntity(query.year());
        Month otherMonth = Month.DECEMBER;
        year.getMonths().add(getMonthWorkloadEntity(otherMonth, DURATION_MINUTES));
        workload.getYears().add(year);
        TrainerWorkloadSummary expected = getTrainerWorkloadSummary(query.year(), query.month(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(workload));
        when(workloadMapper.toTrainerWorkloadSummary(workload, query.year(), query.month(), 0)).thenReturn(expected);

        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlySummary(query);
        assertEquals(expected, result);
        verify(workloadMapper, times(1)).toTrainerWorkloadSummary(workload, query.year(), query.month(), 0);
    }

    @Test
    void getMonthlySummary_ThrowWorkloadNotFoundException_WorkloadMissing() {
        WorkloadQuery query = getWorkloadQuery();

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());
        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.getMonthlySummary(query));
        verify(workloadMapper, never()).toTrainerWorkloadSummary(any(), anyInt(), anyInt(), anyInt());
    }
}
