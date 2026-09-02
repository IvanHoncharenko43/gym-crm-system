package org.example.workload.service;

import org.example.workload.controller.dto.FullName;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.YearWorkloadEntity;
import org.springframework.stereotype.Component;

import java.time.Month;

@Component
public class WorkloadMapper {

    public TrainerWorkloadEntity toTrainerWorkloadEntity(TrainerWorkloadRequest request, TrainerWorkloadEntity existingTrainerWorkloadEntity){
        TrainerWorkloadEntity trainerWorkloadEntity = new TrainerWorkloadEntity();
        trainerWorkloadEntity.setUsername(request.username());
        trainerWorkloadEntity.setFirstName(request.fullName().firstName());
        trainerWorkloadEntity.setLastName(request.fullName().lastName());
        trainerWorkloadEntity.setStatus(request.isActive());
        if(existingTrainerWorkloadEntity != null) {
            trainerWorkloadEntity.setId(existingTrainerWorkloadEntity.getId());
            trainerWorkloadEntity.setYears(existingTrainerWorkloadEntity.getYears());
        }
        return trainerWorkloadEntity;
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

    public YearWorkloadEntity toYearWorkloadEntity(int year){
        YearWorkloadEntity yearWorkloadEntity = new YearWorkloadEntity();
        yearWorkloadEntity.setYear(year);
        return yearWorkloadEntity;
    }

    public MonthWorkloadEntity toMonthWorkloadEntity(Month month){
        MonthWorkloadEntity monthWorkloadEntity = new MonthWorkloadEntity();
        monthWorkloadEntity.setMonth(month);
        return monthWorkloadEntity;
    }
}
