package org.example.training;

import org.example.domain.TrainingType;
import org.example.trainee.TraineeSummary;
import org.example.trainer.TrainerSummary;

import java.time.LocalDate;

public record TrainingResponse(
        Long id,
        TrainerSummary trainer,
        TraineeSummary trainee,
        String trainingName,
        TrainingType trainingType,
        LocalDate trainingDate,
        int duration
) {
}
