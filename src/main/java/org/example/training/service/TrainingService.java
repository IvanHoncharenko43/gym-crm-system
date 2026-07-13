package org.example.training.service;

import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.trainee.dto.GetTraineeTrainingsRequest;
import org.example.trainer.dto.GetTrainerTrainingsRequest;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.core.service.GymMapper;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.repository.TrainerEntity;
import org.example.training.dto.GetTrainingRequest;
import org.example.training.dto.TrainingSummary;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.repository.TrainingEntity;
import org.example.training.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authComponent;

    public TrainingService(TrainingRepository trainingRepository, TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository, GymMapper gymMapper,
                           AuthenticationComponent authComponent){
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.gymMapper = gymMapper;
        this.authComponent = authComponent;
    }

    public TrainingSummary create(CreateTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.getById(request.traineeId())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found or is inactive", request.traineeId());
                    return new EntityNotFoundException("Trainee with ID " + request.traineeId() + " not found or is inactive");
                });

        TrainerEntity trainer = trainerRepository.getById(request.trainerId())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found or is inactive", request.trainerId());
                    return new EntityNotFoundException("Trainer with ID " + request.trainerId() + " not found or is inactive");
                });
        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);
        TrainingEntity savedTraining = trainingRepository.create(training);
        log.info("Created training with ID: {}", savedTraining.getId());
        return gymMapper.toTrainingSummary(savedTraining, trainee, trainer);
    }

    public TrainingSummary getById(GetTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        TrainingEntity training = trainingRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.warn("Training with ID {} not found", request.id());
                    return new EntityNotFoundException("Training with ID " + request.id() + " not found");
                });
        TraineeEntity trainee = traineeRepository.getById(training.getTrainee().getId()).get();
        TrainerEntity trainer = trainerRepository.getById(training.getTrainer().getId()).get();
        log.info("Selected training by ID: {}", request.id());
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    public List<TrainingSummary> getTraineeTrainingList(GetTraineeTrainingsRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        return trainingRepository.findTraineeTrainingsByCriteria(
                request.credentials().username(), request.fromDate(), request.toDate(),
                request.trainerName(), request.trainingType().trainingTypeName()).stream()
                .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                .toList();
    }

    public List<TrainingSummary> getTrainerTrainingList(GetTrainerTrainingsRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authComponent.authenticate(request.credentials());
        return trainingRepository.findTrainerTrainingsByCriteria(
                request.credentials().username(), request.fromDate(),
                        request.toDate(), request.traineeName()).stream()
                .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                .toList();
    }
}
