package org.example.training;

import lombok.Data;
import org.example.shared.Identifiable;
import org.example.shared.TrainingType;

import java.time.LocalDate;

@Data
public class TrainingEntity implements Identifiable {
    private Long id;
    private Long trainerId;
    private Long traineeId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private int durationMinutes;
}
