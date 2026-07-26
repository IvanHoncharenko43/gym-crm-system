package org.example.training.controller.response;

import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainingType.dto.TrainingType;

import java.time.LocalDate;

public record TrainingSummary(
        Long id,
        TrainerSummary trainer,
        TraineeSummary trainee,
        String trainingName,
        TrainingType trainingType,
        LocalDate trainingDate,
        int durationMinutes
) {
}
