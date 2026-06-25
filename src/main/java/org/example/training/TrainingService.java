package org.example.training;

import lombok.extern.slf4j.Slf4j;
import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerRepository;
import org.example.component.GymMapper;
import org.example.trainee.Trainee;
import org.example.trainer.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TrainingService {

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private GymMapper gymMapper;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository){
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper){
        this.gymMapper = gymMapper;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public TrainingResponse create(CreateTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        Trainee trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.error("Trainee with username {} not found", request.traineeUsername());
                    return new IllegalArgumentException("Trainee not found");
                });

        Trainer trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> {
                    log.error("Trainer with username {} not found", request.trainerUsername());
                    return new IllegalArgumentException("Trainer not found");
                });
        Training training = gymMapper.toTraining(request, trainee, trainer);
        Training savedTraining = trainingRepository.create(training);
        log.info("Created training with ID: {}", savedTraining.getId());
        return gymMapper.toTrainingResponse(savedTraining, trainee, trainer);
    }

    public TrainingResponse getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        Training training = trainingRepository.getById(id)
                .orElseThrow(() -> {
                    log.warn("Training with ID {} not found", id);
                    return new IllegalArgumentException("Training with ID " + id + " not found");
                });
        Trainee trainee = traineeRepository.getById(training.getTraineeId())
                .orElseThrow(() -> new IllegalStateException("Trainee for training not found"));
        Trainer trainer = trainerRepository.getById(training.getTrainerId())
                .orElseThrow(() -> new IllegalStateException("Trainer for training not found"));
        log.info("Selected trainer by ID");
        return gymMapper.toTrainingResponse(training, trainee, trainer);
    }
}
