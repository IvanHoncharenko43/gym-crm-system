package org.example.trainee;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.Identifiable;
import org.example.domain.User;

import java.time.LocalDate;

@Getter
@Setter
public class Trainee extends User implements Identifiable {
    private LocalDate dateOfBirth;
    private String address;
}
