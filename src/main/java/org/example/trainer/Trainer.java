package org.example.trainer;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.Identifiable;
import org.example.domain.TrainingType;
import org.example.domain.User;

@Getter
@Setter
public class Trainer extends User implements Identifiable {
    private TrainingType specialization;
}
