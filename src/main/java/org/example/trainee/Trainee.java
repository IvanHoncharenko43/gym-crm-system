package org.example.trainee;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.shared.Identifiable;
import org.example.shared.User;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class Trainee extends User implements Identifiable {
    private LocalDate dateOfBirth;
    private String address;
}
