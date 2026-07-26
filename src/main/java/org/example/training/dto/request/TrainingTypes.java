package org.example.training.dto.request;

import org.example.training.dto.TrainingType;

import java.util.List;

public record TrainingTypes(
        List<TrainingType> trainingType
) {
}
