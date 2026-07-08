package org.example.trainee.dto;

import org.example.user.dto.Credentials;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        Credentials credentials,
        List<String> trainerUsernames
){
}
