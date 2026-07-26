package org.example.training.dto.request;

import org.example.training.dto.TrainingSummary;

import java.util.List;

public record Trainings (
    List<TrainingSummary> trainings
) {
}
