package org.example.workload.service;

import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkload;
import org.example.workload.repository.TrainerWorkloadDocument;
import org.example.workload.repository.TrainerWorkloadRepository;
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
        TrainerWorkloadUpdateEvent event = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadDocument existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 100);
        existingWorkload.getMonths().add(monthWorkload);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, event.trainingDate().getYear())).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadDocument(event, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(event);

        assertEquals(1, existingWorkload.getMonths().size());
        assertEquals(DURATION_MINUTES, existingWorkload.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
        verify(workloadMapper, never()).toMonthWorkload(any());
    }

    @Test
    void updateWorkload_CreateYearAndMonth_ExistingTrainerAndMonthNotFound() {
        TrainerWorkloadUpdateEvent event = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadDocument existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, event.trainingDate().getYear())).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadDocument(event, existingWorkload)).thenReturn(existingWorkload);
        when(workloadMapper.toMonthWorkload(TRAINING_DATE.getMonth())).thenReturn(monthWorkload);

        trainerWorkloadService.updateWorkload(event);

        assertEquals(1, existingWorkload.getMonths().size());
        assertEquals(1, existingWorkload.getMonths().size());
        assertEquals(DURATION_MINUTES, existingWorkload.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
    }

    @Test
    void updateWorkload_CreateTrainerYearAndMonth_TrainerNotFound() {
        TrainerWorkloadUpdateEvent event = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadDocument trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, event.trainingDate().getYear())).thenReturn(Optional.empty());
        when(workloadMapper.toTrainerWorkloadDocument(event, null)).thenReturn(trainerWorkload);
        when(workloadMapper.toMonthWorkload(TRAINING_DATE.getMonth())).thenReturn(monthWorkload);

        trainerWorkloadService.updateWorkload(event);
        assertEquals(1, trainerWorkload.getMonths().size());
        assertEquals(1, trainerWorkload.getMonths().size());
        assertEquals(DURATION_MINUTES, trainerWorkload.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(trainerWorkload);
    }

    @Test
    void updateWorkload_Idempotent_SameAbsoluteValueAppliedTwice() {
        TrainerWorkloadUpdateEvent request = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadDocument existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);
        existingWorkload.getMonths().add(monthWorkload);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, request.trainingDate().getYear())).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadDocument(request, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(request);
        trainerWorkloadService.updateWorkload(request);

        assertEquals(1, existingWorkload.getMonths().size());
        assertEquals(DURATION_MINUTES, existingWorkload.getMonths().iterator().next().getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(2)).save(existingWorkload);
    }

    @Test
    void updateWorkload_OnlyUpdatesTargetedMonth_OtherMonthsUntouched() {
        TrainerWorkloadUpdateEvent event = getTrainerWorkloadRequest(DURATION_MINUTES);
        TrainerWorkloadDocument existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload targetMonth = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), 0);
        MonthWorkload otherMonth = getMonthWorkloadEntity(Month.DECEMBER, 200);
        existingWorkload.getMonths().add(targetMonth);
        existingWorkload.getMonths().add(otherMonth);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, event.trainingDate().getYear())).thenReturn(Optional.of(existingWorkload));
        when(workloadMapper.toTrainerWorkloadDocument(event, existingWorkload)).thenReturn(existingWorkload);

        trainerWorkloadService.updateWorkload(event);

        assertEquals(DURATION_MINUTES, targetMonth.getTrainingSummaryDurationMinutes());
        assertEquals(200, otherMonth.getTrainingSummaryDurationMinutes());
        verify(trainerWorkloadRepository, times(1)).save(existingWorkload);
    }

    @Test
    void getMonthlySummary_ReturnSummaryWithMatchingDuration_YearAndMonthFound() {
        WorkloadQuery request = getWorkloadQuery();
        TrainerWorkloadDocument workload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload month = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        workload.getMonths().add(month);
        TrainerWorkloadSummary expected = getTrainerWorkloadSummary(request.year(), request.month(), DURATION_MINUTES);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, request.year())).thenReturn(Optional.of(workload));
        when(workloadMapper.toTrainerWorkloadSummary(workload, request.year(), request.month(), DURATION_MINUTES)).thenReturn(expected);

        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlySummary(request);
        assertEquals(expected, result);
        verify(workloadMapper, times(1)).toTrainerWorkloadSummary(workload, request.year(), request.month(), DURATION_MINUTES);
    }

    @Test
    void getMonthlySummary_ReturnSummaryWithZeroDuration_MonthNotFoundInYear() {
        WorkloadQuery request = getWorkloadQuery();
        TrainerWorkloadDocument workload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        Month otherMonth = Month.DECEMBER;
        workload.getMonths().add(getMonthWorkloadEntity(otherMonth, DURATION_MINUTES));
        TrainerWorkloadSummary expected = getTrainerWorkloadSummary(request.year(), request.month(), 0);

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, request.year())).thenReturn(Optional.of(workload));
        when(workloadMapper.toTrainerWorkloadSummary(workload, request.year(), request.month(), 0)).thenReturn(expected);

        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlySummary(request);
        assertEquals(expected, result);
        verify(workloadMapper, times(1)).toTrainerWorkloadSummary(workload, request.year(), request.month(), 0);
    }

    @Test
    void getMonthlySummary_ThrowWorkloadNotFoundException_WorkloadMissing() {
        WorkloadQuery request = getWorkloadQuery();

        when(trainerWorkloadRepository.findByUsernameAndYear(TRAINER_USERNAME, request.year())).thenReturn(Optional.empty());
        assertThrows(WorkloadNotFoundException.class, () -> trainerWorkloadService.getMonthlySummary(request));
        verify(workloadMapper, never()).toTrainerWorkloadSummary(any(), anyInt(), anyInt(), anyInt());
    }
}
