package org.example.training.repository;

import lombok.Data;
import org.example.core.repository.Identifiable;
import org.example.training.enums.TrainingType;

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
