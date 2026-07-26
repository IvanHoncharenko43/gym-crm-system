package org.example.trainee.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotEmpty(message = "Trainers usernames list cannot be empty")
        List<@NotBlank(message = "Username in the list cannot be blank") String> trainerUsernames
){
}
