package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkload;
import org.example.workload.repository.TrainerWorkloadDocument;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

import java.time.Month;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadMapper workloadMapper;

    public void updateWorkload(TrainerWorkloadUpdateEvent event){
        int requestYear = event.trainingDate().getYear();
        TrainerWorkloadDocument existingTrainerWorkloadDocument = trainerWorkloadRepository.findByUsernameAndYear(event.username(), requestYear)
                .orElse(null);
        TrainerWorkloadDocument trainerWorkloadDocument = workloadMapper.toTrainerWorkloadDocument(event, existingTrainerWorkloadDocument);
        Month requestMonth = event.trainingDate().getMonth();
        MonthWorkload monthWorkload = trainerWorkloadDocument.getMonths().stream()
                .filter(m -> m.getMonth() == requestMonth)
                .findFirst()
                .orElseGet(() -> {
                    MonthWorkload createdMonthWorkload = workloadMapper.toMonthWorkload(requestMonth);
                    trainerWorkloadDocument.getMonths().add(createdMonthWorkload);
                    return createdMonthWorkload;
                });
        monthWorkload.setTrainingSummaryDurationMinutes(event.trainingSummaryDurationMinutes());
        trainerWorkloadRepository.save(trainerWorkloadDocument);
        log.info("Upserted trainer's {} workload to {} minutes", event.trainingDate(), event.trainingSummaryDurationMinutes());
    }

    public TrainerWorkloadSummary getMonthlySummary(WorkloadQuery query){
        int requestYear = query.year();
        TrainerWorkloadDocument workload = trainerWorkloadRepository.findByUsernameAndYear(query.username(), requestYear)
                .orElseThrow(() -> new WorkloadNotFoundException("No workload found for trainer"));
        Month requestedMonth = Month.of(query.month());
        int durationMinutes = workload.getMonths().stream()
                .filter(m -> m.getMonth() == requestedMonth)
                .findFirst()
                .map(MonthWorkload::getTrainingSummaryDurationMinutes)
                .orElse(0);
        return workloadMapper.toTrainerWorkloadSummary(workload, query.year(), query.month(), durationMinutes);
    }
}
