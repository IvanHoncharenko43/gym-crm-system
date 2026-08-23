package org.example.workload.service;

import org.example.workload.controller.dto.FullName;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkloadMapper {

    public TrainerWorkloadSummary toTrainerWorkloadSummary(TrainerWorkloadEntity workload, int year, int month, int durationMinutes) {
        return new TrainerWorkloadSummary(
                workload.getUsername(),
                new FullName(workload.getFirstName(), workload.getLastName()),
                workload.isStatus(),
                year,
                month,
                durationMinutes
        );
    }
}
