package org.example.workload.service;

import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
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
    void updateWorkload_OverwriteExistingMonthDuration_ExistingYearAndMonthFound() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
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
        assertEquals(DURATION_MINUTES, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
        verify(workloadMapper, never()).toYearWorkloadEntity(anyInt());
        verify(workloadMapper, never()).toMonthWorkloadEntity(any());
    }

    @Test
    void updateWorkload_CreateYearAndMonth_ExistingTrainerYearNotFound() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);
        when(workloadMapper.toYearWorkloadEntity(TRAINING_DATE.getYear()))
                .thenReturn(yearWorkload);
        when(workloadMapper.toMonthWorkloadEntity(TRAINING_DATE.getMonth()))
                .thenReturn(monthWorkload);

        trainerWorkloadService.updateWorkload(request);

        assertEquals(1, existingWorkload.getYears().size());
        YearWorkloadEntity createdYear = existingWorkload.getYears().iterator().next();
        assertEquals(1, createdYear.getMonths().size());
        assertEquals(DURATION_MINUTES, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
    }

    @Test
    void updateWorkload_CreateTrainerYearAndMonth_TrainerNotFound() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());
        when(workloadMapper.toTrainerWorkloadEntity(request, null)).thenReturn(trainerWorkload);
        when(workloadMapper.toYearWorkloadEntity(TRAINING_DATE.getYear()))
                .thenReturn(yearWorkload);
        when(workloadMapper.toMonthWorkloadEntity(TRAINING_DATE.getMonth()))
                .thenReturn(monthWorkload);

        trainerWorkloadService.updateWorkload(request);
        assertEquals(1, trainerWorkload.getYears().size());
        YearWorkloadEntity createdYear = trainerWorkload.getYears().iterator().next();
        assertEquals(1, createdYear.getMonths().size());
        assertEquals(DURATION_MINUTES, createdYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(trainerWorkload);
    }

    @Test
    void updateWorkload_Idempotent_SameAbsoluteValueAppliedTwice() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);
        yearWorkload.getMonths().add(monthWorkload);
        existingWorkload.getYears().add(yearWorkload);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(request);
        trainerWorkloadService.updateWorkload(request);

        assertEquals(1, existingWorkload.getYears().size());
        YearWorkloadEntity theOnlyYear = existingWorkload.getYears().iterator().next();
        assertEquals(1, theOnlyYear.getMonths().size());
        assertEquals(DURATION_MINUTES, theOnlyYear.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(2)).save(existingWorkload);
    }

    @Test
    void updateWorkload_OnlyUpdatesTargetedMonth_OtherMonthsAndYearsUntouched() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity targetYear = getYearWorkloadEntity(TRAINING_DATE.getYear());
        MonthWorkloadEntity targetMonth = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);
        MonthWorkloadEntity otherMonth = getMonthWorkloadEntity(Month.DECEMBER, 200);
        targetYear.getMonths().add(targetMonth);
        targetYear.getMonths().add(otherMonth);
        YearWorkloadEntity otherYear = getYearWorkloadEntity(TRAINING_DATE.getYear() - 1);
        MonthWorkloadEntity otherYearMonth = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 300);
        otherYear.getMonths().add(otherYearMonth);
        existingWorkload.getYears().add(targetYear);
        existingWorkload.getYears().add(otherYear);

        when(trainerWorkloadRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadEntity(request, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(request);

        assertEquals(DURATION_MINUTES, targetMonth.getTrainingSummaryDurationMinutes());
        assertEquals(200, otherMonth.getTrainingSummaryDurationMinutes());
        assertEquals(300, otherYearMonth.getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
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
