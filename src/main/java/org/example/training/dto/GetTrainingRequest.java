package org.example.training.dto;

import org.example.user.dto.UserCredentials;

public record GetTrainingRequest(
        UserCredentials credentials,
        Long id
){
}
