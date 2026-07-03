package org.example.training;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerRepository;
import org.example.shared.GymMapper;
import org.example.trainee.TraineeEntity;
import org.example.trainer.TrainerEntity;
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

    public TrainingSummary create(CreateTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TraineeEntity trainee = traineeRepository.getById(request.traineeId())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.traineeId());
                    return new NotFoundException("Trainee with ID " + request.traineeId() + " not found");
                });

        TrainerEntity trainer = trainerRepository.getById(request.trainerId())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found", request.trainerId());
                    return new NotFoundException("Trainer with ID " + request.trainerId() + " not found");
                });
        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);
        TrainingEntity savedTraining = trainingRepository.create(training);
        log.info("Created training with ID: {}", savedTraining.getId());
        return gymMapper.toTrainingSummary(savedTraining, trainee, trainer);
    }

    public TrainingSummary getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        TrainingEntity training = trainingRepository.getById(id)
                .orElseThrow(() -> {
                    log.warn("Training with ID {} not found", id);
                    return new NotFoundException("Training with ID " + id + " not found");
                });
        TraineeEntity trainee = traineeRepository.getById(training.getTraineeId())
                .orElseThrow(() -> {
                    log.error("Trainee for training {} not found", id);
                    return new NotFoundException("Trainee for training with ID " + id + " not found");
                });
        TrainerEntity trainer = trainerRepository.getById(training.getTrainerId())
                .orElseThrow(() -> {
                    log.error("Trainer for training with ID {} not found", id);
                    return new NotFoundException("Trainer for training with ID " + id + " not found");
                });
        log.info("Selected training by ID: {}", id);
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }
}
