package org.example.trainer.dto;

import org.example.user.dto.UserCredentials;

public record UnassignedTrainersRequest(
        UserCredentials credentials,
        String traineeUsername
) {
}
