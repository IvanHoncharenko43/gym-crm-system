package org.example.training.repository;

import java.time.LocalDate;
import java.util.List;

public interface CustomTrainingRepository {

    List<TrainingEntity> findTraineeTrainings(String traineeUsername, LocalDate fromDate,
                                              LocalDate toDate, String trainerName, String trainingTypeName);

    List<TrainingEntity> findTrainerTrainings(String trainerUsername, LocalDate fromDate,
                                              LocalDate toDate, String traineeName);
}
