package org.example.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Detailed profile information of the user")
public record UserProfile (

        @Schema(description = "Username of the user")
        @NotBlank(message = "Username cannot be blank")
        String username
){
}
