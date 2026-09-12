package org.example.workload.service;

import org.example.workload.controller.dto.FullName;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkload;
import org.example.workload.repository.TrainerWorkloadDocument;
import org.springframework.stereotype.Component;

import java.time.Month;

@Component
public class WorkloadMapper {

    public TrainerWorkloadDocument toTrainerWorkloadDocument(TrainerWorkloadUpdateEvent event, TrainerWorkloadDocument existingTrainerWorkloadDocument){
        TrainerWorkloadDocument trainerWorkloadDocument = new TrainerWorkloadDocument();
        trainerWorkloadDocument.setUsername(event.username());
        trainerWorkloadDocument.setFirstName(event.fullName().firstName());
        trainerWorkloadDocument.setLastName(event.fullName().lastName());
        trainerWorkloadDocument.setStatus(event.isActive());
        trainerWorkloadDocument.setYear(event.trainingDate().getYear());
        if(existingTrainerWorkloadDocument != null) {
            trainerWorkloadDocument.setId(existingTrainerWorkloadDocument.getId());
            trainerWorkloadDocument.setMonths(existingTrainerWorkloadDocument.getMonths());
            trainerWorkloadDocument.setCreatedAt(existingTrainerWorkloadDocument.getCreatedAt());
        }
        return trainerWorkloadDocument;
    }

    public TrainerWorkloadSummary toTrainerWorkloadSummary(TrainerWorkloadDocument workload, int year, int month, int durationMinutes) {
        return new TrainerWorkloadSummary(
                workload.getUsername(),
                new FullName(workload.getFirstName(), workload.getLastName()),
                workload.isStatus(),
                year,
                month,
                durationMinutes
        );
    }

    public MonthWorkload toMonthWorkload(Month month){
        MonthWorkload monthWorkload = new MonthWorkload();
        monthWorkload.setMonth(month);
        return monthWorkload;
    }
}
