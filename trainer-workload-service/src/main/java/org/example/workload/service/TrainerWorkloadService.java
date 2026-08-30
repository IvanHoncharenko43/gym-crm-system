package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.exception.InvalidStateTransitionException;
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
    public void updateWorkload(TrainerWorkloadRequest request){
        boolean isDelete = request.actionType() == ActionType.DELETE;
        TrainerWorkloadEntity existingTrainerWorkloadEntity = trainerWorkloadRepository.findByUsername(request.username())
                        .orElseGet(() -> {
                            if (isDelete) {
                                throw new WorkloadNotFoundException("Cannot update-delete trainer's workload because there isn't one to alter");
                            }
                            return null;
                        });
        TrainerWorkloadEntity trainerWorkloadEntity = workloadMapper.toTrainerWorkloadEntity(request, existingTrainerWorkloadEntity);

        int requestYear = request.trainingDate().getYear();
        YearWorkloadEntity yearWorkloadEntity = trainerWorkloadEntity.getYears().stream()
                .filter(y -> y.getYear() == requestYear)
                .findFirst()
                .orElseGet(() -> {
                    if (isDelete) {
                        throw new WorkloadNotFoundException(
                                "Cannot update-delete trainer's %s workload because there isn't enough year data to alter".formatted(requestYear));
                    }
                    return workloadMapper.toYearWorkloadEntity(requestYear, trainerWorkloadEntity);
                });
        Month requestMonth = request.trainingDate().getMonth();
        MonthWorkloadEntity monthWorkloadEntity = yearWorkloadEntity.getMonths().stream()
                .filter(m -> m.getMonth() == requestMonth)
                .findFirst()
                .orElseGet(() -> {
                    if (isDelete) {
                        throw new WorkloadNotFoundException(String.format(
                                "Cannot update-delete trainer's %s workload because there isn't enough month data to alter", request.trainingDate()));
                    }
                    return workloadMapper.toMonthWorkloadEntity(requestMonth, yearWorkloadEntity);
                });
        int currentMinutes = monthWorkloadEntity.getTrainingSummaryDurationMinutes();
        int addend = isDelete ? -request.trainingSummaryDurationMinutes() : request.trainingSummaryDurationMinutes();
        int updatedMinutes = currentMinutes + addend;
        if (updatedMinutes < 0) {
            throw new InvalidStateTransitionException(String.format(
                    "Cannot update trainer's %s workload because resulting workload cannot go negative", request.trainingDate()));
        }
        monthWorkloadEntity.setTrainingSummaryDurationMinutes(updatedMinutes);
        trainerWorkloadRepository.save(trainerWorkloadEntity);
        log.info("Updated a trainer's workload with operation: {}", request.actionType().name());
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
