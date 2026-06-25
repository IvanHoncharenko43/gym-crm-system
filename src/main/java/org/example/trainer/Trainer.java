package org.example.trainer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.shared.Identifiable;
import org.example.shared.TrainingType;
import org.example.shared.User;

@EqualsAndHashCode(callSuper = true)
@Data
public class Trainer extends User implements Identifiable {
    private TrainingType specialization;
}
