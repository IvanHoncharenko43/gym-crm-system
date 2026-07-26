package org.example.trainer.controller.response;

import java.util.List;

public record Trainers(
        List<TrainerSummary> trainers
) {
}
