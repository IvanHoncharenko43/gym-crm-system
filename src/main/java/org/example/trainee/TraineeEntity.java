package org.example.trainee;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.shared.Identifiable;
import org.example.shared.UserEntity;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class TraineeEntity extends UserEntity implements Identifiable {
    private LocalDate dateOfBirth;
    private String address;
}
