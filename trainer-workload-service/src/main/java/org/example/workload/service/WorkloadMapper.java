package org.example.workload.service;

import org.example.workload.controller.dto.FullName;
import org.example.workload.messaging.TrainerWorkloadUpdateEvent;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.springframework.stereotype.Component;

import java.time.Month;

@Component
public class WorkloadMapper {

    public TrainerWorkloadEntity toTrainerWorkloadDocument(TrainerWorkloadUpdateEvent event, TrainerWorkloadEntity existingTrainerWorkloadDocument){
        TrainerWorkloadEntity trainerWorkloadDocument = new TrainerWorkloadEntity();
        trainerWorkloadDocument.setUsername(event.username());
        trainerWorkloadDocument.setFirstName(event.fullName().firstName());
        trainerWorkloadDocument.setLastName(event.fullName().lastName());
        trainerWorkloadDocument.setStatus(event.isActive());
        trainerWorkloadDocument.setYear(event.trainingDate().getYear());
        if(existingTrainerWorkloadDocument != null) {
            trainerWorkloadDocument.setId(existingTrainerWorkloadDocument.getId());
            trainerWorkloadDocument.setMonths(existingTrainerWorkloadDocument.getMonths());
        }
        return trainerWorkloadDocument;
    }

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

    public MonthWorkloadEntity toMonthWorkload(Month month){
        MonthWorkloadEntity monthWorkload = new MonthWorkloadEntity();
        monthWorkload.setMonth(month);
        return monthWorkload;
    }
}
