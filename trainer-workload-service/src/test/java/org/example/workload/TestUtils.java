package org.example.workload;

import org.example.workload.controller.dto.ActionType;
import org.example.workload.controller.dto.FullName;
import org.example.workload.controller.dto.request.TrainerWorkloadRequest;
import org.example.workload.controller.dto.request.WorkloadQuery;
import org.example.workload.controller.dto.response.TrainerWorkloadSummary;
import org.example.workload.repository.MonthWorkloadEntity;
import org.example.workload.repository.TrainerWorkloadEntity;
import org.example.workload.repository.YearWorkloadEntity;

import java.time.LocalDate;
import java.time.Month;

public class TestUtils {

    public static final String TRAINER_USERNAME = "John.Doe";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final LocalDate TRAINING_DATE = LocalDate.of(2026, 5, 12);
    public static final int DURATION_MINUTES = 60;

    public static TrainerWorkloadRequest getTrainerWorkloadRequest() {
        return getTrainerWorkloadRequest(ActionType.ADD, DURATION_MINUTES);
    }

    public static TrainerWorkloadRequest getTrainerWorkloadRequest(ActionType actionType, int durationMinutes) {
        return new TrainerWorkloadRequest(
                TRAINER_USERNAME,
                new FullName(FIRST_NAME, LAST_NAME),
                true,
                TRAINING_DATE,
                durationMinutes,
                actionType
        );
    }

    public static TrainerWorkloadRequest getTrainerWorkloadRequest(LocalDate trainingDate, ActionType actionType, int durationMinutes) {
        return new TrainerWorkloadRequest(
                TRAINER_USERNAME,
                new FullName(FIRST_NAME, LAST_NAME),
                true,
                trainingDate,
                durationMinutes,
                actionType
        );
    }

    public static TrainerWorkloadEntity getTrainerWorkloadEntity(String username, String firstName, String lastName, boolean status) {
        TrainerWorkloadEntity entity = new TrainerWorkloadEntity();
        entity.setUsername(username);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setStatus(status);
        return entity;
    }

    public static YearWorkloadEntity getYearWorkloadEntity(int year) {
        YearWorkloadEntity entity = new YearWorkloadEntity();
        entity.setYear(year);
        return entity;
    }

    public static MonthWorkloadEntity getMonthWorkloadEntity(Month month, int trainingSummaryDurationMinutes) {
        MonthWorkloadEntity entity = new MonthWorkloadEntity();
        entity.setMonth(month);
        entity.setTrainingSummaryDurationMinutes(trainingSummaryDurationMinutes);
        return entity;
    }

    public static WorkloadQuery getWorkloadQuery() {
        return getWorkloadQuery(TRAINER_USERNAME, TRAINING_DATE.getYear(), TRAINING_DATE.getMonthValue());
    }

    public static WorkloadQuery getWorkloadQuery(String username, int year, int month) {
        return new WorkloadQuery(username, year, month);
    }

    public static TrainerWorkloadSummary getTrainerWorkloadSummary(int year, int month, int durationMinutes) {
        return new TrainerWorkloadSummary(
                TRAINER_USERNAME, new FullName(FIRST_NAME, LAST_NAME), true, year, month, durationMinutes
        );
    }
}
