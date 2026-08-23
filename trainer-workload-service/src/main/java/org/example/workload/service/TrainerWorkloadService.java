package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

import java.time.Month;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadAggregator workloadAggregator;
    private final WorkloadMapper workloadMapper;

    public void updateWorkload(TrainerWorkloadRequest request){
        trainerWorkloadRepository.computeAndSave(request.username(), existing -> workloadAggregator.apply(existing, request));
        log.info("Updated a trainer's workload with operation: {}", request.actionType().name());
    }

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
