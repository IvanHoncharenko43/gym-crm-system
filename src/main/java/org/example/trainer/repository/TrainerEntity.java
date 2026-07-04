package org.example.trainer.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.core.repository.Identifiable;
import org.example.training.enums.TrainingType;
import org.example.user.repository.UserEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainerEntity extends UserEntity implements Identifiable {
    private TrainingType specialization;
}
