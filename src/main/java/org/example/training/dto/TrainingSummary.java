package org.example.training.dto;

import org.example.trainee.dto.TraineeSummary;
import org.example.trainer.dto.TrainerSummary;

import java.time.LocalDate;

public record TrainingSummary(
        Long id,
        TrainerSummary trainer,
        TraineeSummary trainee,
        String trainingName,
        TrainingTypeSummary trainingType,
        LocalDate trainingDate,
        int durationMinutes
) {
}
