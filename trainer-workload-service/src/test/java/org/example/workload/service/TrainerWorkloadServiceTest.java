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

import static org.example.workload.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private WorkloadMapper workloadMapper;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateWorkload_IncrementExistingMonthDuration_ExistingYearAndMonthFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 100);
        yearWorkload.getMonths().add(monthWorkload);
        existingWorkload.getYears().add(yearWorkload);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(request);

        assertEquals(1, existingWorkload.getYears().size());
        YearWorkloadEntity createdYear = existingWorkload.getYears().iterator().next();
        assertEquals(1, createdYear.getMonths().size());
        assertEquals(160, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
        verify(workloadMapper, never()).toYearWorkloadEntity(anyInt(), any());
        verify(workloadMapper, never()).toMonthWorkloadEntity(any(), any());
    }

    @Test
    void updateWorkload_CreateYearAndMonth_ExistingTrainerYearNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);
        when(workloadMapper.toYearWorkloadEntity(TRAINING_DATE.getYear(), existingWorkload))
                .thenAnswer(invocation -> {
                    yearWorkload.setTrainerWorkload(existingWorkload);
                    existingWorkload.getYears().add(yearWorkload);
                    return yearWorkload;
                });
        when(workloadMapper.toMonthWorkloadEntity(TRAINING_DATE.getMonth(), yearWorkload))
                .thenAnswer(invocation -> {
                    monthWorkload.setYearWorkload(yearWorkload);
                    yearWorkload.getMonths().add(monthWorkload);
                    return monthWorkload;
                });

        trainerWorkloadService.updateWorkload(request);

        assertEquals(1, existingWorkload.getYears().size());
        YearWorkloadEntity createdYear = existingWorkload.getYears().iterator().next();
        assertEquals(1, createdYear.getMonths().size());
        assertEquals(DURATION_MINUTES, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
    }

    @Test
    void updateWorkload_CreateTrainerYearAndMonth_TrainerNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());
        when(workloadMapper.toTrainerWorkloadEntity(request, null)).thenReturn(trainerWorkload);
        when(workloadMapper.toYearWorkloadEntity(TRAINING_DATE.getYear(), trainerWorkload))
                .thenAnswer(invocation -> {
                    yearWorkload.setTrainerWorkload(trainerWorkload);
                    trainerWorkload.getYears().add(yearWorkload);
                    return yearWorkload;
                });
        when(workloadMapper.toMonthWorkloadEntity(eq(TRAINING_DATE.getMonth()), any(YearWorkloadEntity.class)))
                .thenAnswer(invocation -> {
                    monthWorkload.setYearWorkload(yearWorkload);
                    yearWorkload.getMonths().add(monthWorkload);
                    return monthWorkload;
                });

        trainerWorkloadService.updateWorkload(request);
        assertEquals(1, trainerWorkload.getYears().size());
        YearWorkloadEntity createdYear = trainerWorkload.getYears().iterator().next();
        assertEquals(1, createdYear.getMonths().size());
        assertEquals(DURATION_MINUTES, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(trainerWorkload);
    }

    @Test
    void updateWorkload_ThrowWorkloadNotFoundException_DeleteAndTrainerNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.updateWorkload(request));
        verify(workloadMapper, never()).toTrainerWorkloadEntity(any(), any());
        verify(trainerWorkloadRepository, never()).save(any());
    }

    @Test
    void updateWorkload_ThrowWorkloadNotFoundException_DeleteAndYearNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.updateWorkload(request));
        verify(workloadMapper, never()).toYearWorkloadEntity(anyInt(), any());
        verify(trainerWorkloadRepository, never()).save(any());
    }

    @Test
    void updateWorkload_ThrowWorkloadNotFoundException_DeleteAndMonthNotFound() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        existingWorkload.getYears().add(yearWorkload);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.updateWorkload(request));
        verify(workloadMapper, never()).toMonthWorkloadEntity(any(), any());
        verify(trainerWorkloadRepository, never()).save(any());
    }

    @Test
    void updateWorkload_ThrowInvalidStateTransitionException_DeleteResultsInNegativeDuration() {
        TrainerWorkloadRequest request = getTrainerWorkloadRequest(ActionType.DELETE, DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES - 40);
        yearWorkload.getMonths().add(monthWorkload);
        existingWorkload.getYears().add(yearWorkload);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        assertThrows(InvalidStateTransitionException.class, () -> trainerWorkloadService.updateWorkload(request));
        verify(trainerWorkloadRepository, never()).save(any());
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
