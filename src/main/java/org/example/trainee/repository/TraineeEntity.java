package org.example.trainee.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.core.repository.Identifiable;
import org.example.user.repository.UserEntity;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class TraineeEntity extends UserEntity implements Identifiable {
    private LocalDate dateOfBirth;
    private String address;
}
