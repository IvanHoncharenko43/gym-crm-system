package org.example.workload.service;

import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.TrainerWorkloadRequest;
import org.example.workload.exception.InvalidStateTransitionException;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.YearWorkloadEntity;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.Optional;

@Component
public class WorkloadAggregator {

    public TrainerWorkloadEntity apply(TrainerWorkloadEntity existing, TrainerWorkloadRequest request) {
        boolean isDelete = request.actionType() == ActionType.DELETE;
        if (isDelete && existing == null) {
            throw new WorkloadNotFoundException("Cannot update-delete trainer's workload because there isn't one to alter");
        }
        Optional<YearWorkloadEntity> existingYear = existing == null
                ? Optional.empty()
                : findYear(existing, request.trainingDate().getYear());
        Optional<MonthWorkloadEntity> existingMonth = existingYear
                .flatMap(year -> findMonth(year, request.trainingDate().getMonth()));
        if (isDelete && existingMonth.isEmpty()) {
            throw new WorkloadNotFoundException(
                    String.format("Cannot update-delete trainer's %s workload because there isn't enough month data to alter", request.trainingDate()));
        }
        int currentMinutes = existingMonth.map(MonthWorkloadEntity::getTrainingSummaryDurationMinutes)
                .orElse(0);
        int addend = isDelete ? -request.trainingSummaryDurationMinutes() : request.trainingSummaryDurationMinutes();
        int updatedMinutes = currentMinutes + addend;
        if (updatedMinutes < 0) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot update trainer's %s workload because resulting workload cannot go negative", request.trainingDate()));
        }
        TrainerWorkloadEntity target = existing != null ? existing : newEntity(request);
        target.setFirstName(request.fullName().firstName());
        target.setLastName(request.fullName().lastName());
        target.setStatus(request.isActive());
        YearWorkloadEntity year = existingYear.orElseGet(() -> createYear(target, request.trainingDate().getYear()));
        MonthWorkloadEntity month = existingMonth.orElseGet(() -> createMonth(year, request.trainingDate().getMonth()));
        month.setTrainingSummaryDurationMinutes(updatedMinutes);
        return target;
    }

    private Optional<YearWorkloadEntity> findYear(TrainerWorkloadEntity entity, int year) {
        return entity.getYears().stream().filter(y -> y.getYear() == year).findFirst();
    }

    private YearWorkloadEntity createYear(TrainerWorkloadEntity entity, int year) {
        YearWorkloadEntity created = new YearWorkloadEntity();
        created.setYear(year);
        entity.getYears().add(created);
        return created;
    }

    private Optional<MonthWorkloadEntity> findMonth(YearWorkloadEntity year, Month month) {
        return year.getMonths().stream().filter(m -> m.getMonth() == month).findFirst();
    }

    private MonthWorkloadEntity createMonth(YearWorkloadEntity year, Month month) {
        MonthWorkloadEntity created = new MonthWorkloadEntity();
        created.setMonth(month);
        year.getMonths().add(created);
        return created;
    }

    private TrainerWorkloadEntity newEntity(TrainerWorkloadRequest request) {
        TrainerWorkloadEntity entity = new TrainerWorkloadEntity();
        entity.setUsername(request.username());
        return entity;
    }
}
