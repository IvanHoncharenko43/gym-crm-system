package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.example.workload.repository.YearWorkloadEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadMapper workloadMapper;

    @Transactional
    public void updateWorkload(TrainerWorkloadUpdateEvent event){
        TrainerWorkloadEntity existingTrainerWorkloadEntity = trainerWorkloadRepository.findByUsername(event.username())
                .orElse(null);
        TrainerWorkloadEntity trainerWorkloadEntity = workloadMapper.toTrainerWorkloadEntity(event, existingTrainerWorkloadEntity);

        int requestYear = event.trainingDate().getYear();
        YearWorkloadEntity yearWorkloadEntity = trainerWorkloadEntity.getYears().stream()
                .filter(y -> y.getYear() == requestYear)
                .findFirst()
                .orElseGet(() -> {
                    YearWorkloadEntity createdYearWorkload = workloadMapper.toYearWorkloadEntity(requestYear);
                    trainerWorkloadEntity.getYears().add(createdYearWorkload);
                    return createdYearWorkload;
                });
        Month requestMonth = event.trainingDate().getMonth();
        MonthWorkloadEntity monthWorkloadEntity = yearWorkloadEntity.getMonths().stream()
                .filter(m -> m.getMonth() == requestMonth)
                .findFirst()
                .orElseGet(() -> {
                    MonthWorkloadEntity createdMonthWorkload = workloadMapper.toMonthWorkloadEntity(requestMonth);
                    yearWorkloadEntity.getMonths().add(createdMonthWorkload);
                    return createdMonthWorkload;
                });
        monthWorkloadEntity.setTrainingSummaryDurationMinutes(event.trainingSummaryDurationMinutes());
        trainerWorkloadRepository.save(trainerWorkloadEntity);
        log.info("Upserted trainer's {} workload to {} minutes", event.trainingDate(), event.trainingSummaryDurationMinutes());
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadSummary getMonthlySummary(WorkloadQuery query){
        TrainerWorkloadEntity workload = trainerWorkloadRepository.findByUsername(query.username())
                .orElseThrow(() -> new WorkloadNotFoundException("No workload found for trainer"));
        Month requestedMonth = Month.of(query.month());
        int durationMinutes = workload.getYears().stream()
                .filter(y -> y.getYear() == query.year())
                .findFirst()
                .flatMap(y -> y.getMonths().stream()
                        .filter(m -> m.getMonth() == requestedMonth)
                        .findFirst())
                .map(MonthWorkloadEntity::getTrainingSummaryDurationMinutes)
                .orElse(0);
        return workloadMapper.toTrainerWorkloadSummary(workload, query.year(), query.month(), durationMinutes);
    }
}
