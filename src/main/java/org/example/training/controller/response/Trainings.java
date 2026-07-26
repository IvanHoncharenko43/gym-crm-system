package org.example.training.controller.response;

import java.util.List;

public record Trainings (
    List<TrainingSummary> trainings
) {
}
