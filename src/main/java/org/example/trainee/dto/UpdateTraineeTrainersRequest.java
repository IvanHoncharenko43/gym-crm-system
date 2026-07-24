package org.example.trainee.dto;

import org.example.user.dto.UserCredentials;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        UserCredentials credentials,
        List<String> trainerUsernames
){
}
