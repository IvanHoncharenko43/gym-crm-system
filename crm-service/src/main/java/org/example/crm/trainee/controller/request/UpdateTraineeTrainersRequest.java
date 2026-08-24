package org.example.crm.trainee.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "DTO for updating trainee's trainers list", name = "UpdateTraineeTrainers")
public record UpdateTraineeTrainersRequest(

        @Schema(description = "List of trainers to assign to trainee", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Trainers usernames list cannot be empty")
        List<@NotBlank(message = "Username in the list cannot be blank") String> trainerUsernames
){
}
