package org.example.trainer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.shared.Identifiable;
import org.example.shared.TrainingType;
import org.example.shared.UserEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainerEntity extends UserEntity implements Identifiable {
    private TrainingType specialization;
}
