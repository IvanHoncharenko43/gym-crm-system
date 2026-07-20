package org.example.training.dto;

import org.example.user.dto.UserCredentials;

public record GetTrainingRequest(
        Long id,
        UserCredentials credentials
){
}
