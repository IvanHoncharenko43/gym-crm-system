package org.example.trainer.dto;

import org.example.user.dto.Credentials;

public record UnassignedTrainersRequest(
        Credentials credentials,
        String traineeUsername
) {
}
