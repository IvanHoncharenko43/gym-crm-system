package org.example.training;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.Identifiable;
import org.example.domain.TrainingType;

import java.time.LocalDate;

@Getter
@Setter
public class Training implements Identifiable {
    private Long id;
    private Long trainerId;
    private Long traineeId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private int duration;
}
