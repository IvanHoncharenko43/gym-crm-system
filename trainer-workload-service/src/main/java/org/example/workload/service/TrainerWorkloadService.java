package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.controller.dto.TrainerWorkloadRequest;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final WorkloadAggregator workloadAggregator;

    public void updateWorkload(TrainerWorkloadRequest request){
        TrainerWorkloadEntity existingWorkload = trainerWorkloadRepository
                .findByUsername(request.username())
                .orElse(null);
        TrainerWorkloadEntity updatedWorkload = workloadAggregator.apply(existingWorkload, request);
        trainerWorkloadRepository.save(updatedWorkload);
        log.info("Updated a trainer's workload with operation: {}", request.actionType().name());
    }
}
