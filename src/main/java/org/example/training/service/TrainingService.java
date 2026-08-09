package org.example.training.service;

import org.example.monitoring.TransactionalMetricService;
import org.example.exception.InvalidRequestDataException;
import org.example.training.controller.response.Trainings;
import org.example.training.repository.TrainingSpecifications;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.exception.EntityNotFoundException;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerRepository;
import org.example.core.service.GymMapper;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.repository.TrainerEntity;
import org.example.training.controller.response.TrainingSummary;
import org.example.training.controller.request.CreateTrainingRequest;
import org.example.training.repository.TrainingEntity;
import org.example.training.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;

@Slf4j
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;
    private final TransactionalMetricService transactionalMetricService;

    public TrainingService(TrainingRepository trainingRepository, TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository, GymMapper gymMapper,
                           AuthenticationComponent authenticator, TransactionalMetricService transactionalMetricService){
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
        this.transactionalMetricService = transactionalMetricService;
    }

    @Transactional
    public TrainingSummary create(CreateTrainingRequest request, UserCredentials credentials) {
        authenticator.authenticate(credentials);
        TraineeEntity trainee = traineeRepository.findByUsername(request.traineeUsername())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = "Trainee not found or is inactive";
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });

        TrainerEntity trainer = trainerRepository.findByUsername(request.trainerUsername())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = "Trainer not found or is inactive";
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });
        trainee.getTrainers().add(trainer);
        trainer.getTrainees().add(trainee);
        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);
        TrainingEntity savedTraining = trainingRepository.save(training);
        log.info("Created training with ID: {}", savedTraining.getId());
        transactionalMetricService.incrementOnCommit("training_sessions_booked_total", "type", training.getTrainingType().getTrainingTypeName().name());
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    @Transactional(readOnly = true)
    public TrainingSummary getById(Long id, UserCredentials credentials) {
        authenticator.authenticate(credentials);
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Training with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        TraineeEntity trainee = traineeRepository.findById(training.getTrainee().getId()).get();
        TrainerEntity trainer = trainerRepository.findById(training.getTrainer().getId()).get();
        log.info("Selected training by ID: {}", id);
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    @Transactional(readOnly = true)
    public Trainings getTraineeTrainingList(GetTraineeTrainingsRequest request, UserCredentials credentials){
        authenticator.authenticate(credentials);
        Specification<TrainingEntity> specification = TrainingSpecifications.findTraineeTrainings(request.getUsername(),
                request.getFromDate(), request.getToDate(), request.getTrainerName(), request.getTrainingType().name());
        return new Trainings(
                trainingRepository.findAll(specification).stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Trainings getTrainerTrainingList(GetTrainerTrainingsRequest request, UserCredentials credentials){
        authenticator.authenticate(credentials);
        Specification<TrainingEntity> specification = TrainingSpecifications.findTrainerTrainings(request.username(),
                request.fromDate(), request.toDate(), request.traineeName());
        return new Trainings(
                trainingRepository.findAll(specification).stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }
}
