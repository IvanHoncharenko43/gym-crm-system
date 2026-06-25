package org.example.trainee;


import java.time.LocalDate;

public record CreateTraineeRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address
) {
}
