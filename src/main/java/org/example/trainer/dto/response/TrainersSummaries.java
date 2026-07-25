package org.example.trainer.dto.response;

import org.example.trainer.dto.TrainerSummary;

import java.util.List;

public record TrainersSummaries(
        List<TrainerSummary> trainerSummaries
) {
}
