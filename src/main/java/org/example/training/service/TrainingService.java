package org.example.training.service;

import org.example.exception.InvalidRequestDataException;
import org.example.training.controller.response.Trainings;
import org.example.user.controller.dto.UserCredentials;
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

@Slf4j
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;

    public TrainingService(TrainingRepository trainingRepository, TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository, GymMapper gymMapper,
                           AuthenticationComponent authenticator){
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
    }

    @Transactional
    public void create(CreateTrainingRequest request, UserCredentials credentials) {
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
        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);
        TrainingEntity savedTraining = trainingRepository.save(training);
        log.info("Created training with ID: {}", savedTraining.getId());
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
        return new Trainings(
                trainingRepository.findTraineeTrainingsByCriteria(request.username(), request.fromDate(), request.toDate(),
                                request.trainerName(), request.trainingType().name()).stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Trainings getTrainerTrainingList(GetTrainerTrainingsRequest request, UserCredentials credentials){
        authenticator.authenticate(credentials);
        return new Trainings(
                trainingRepository.findTrainerTrainingsByCriteria(request.username(), request.fromDate(),
                                request.toDate(), request.traineeName()).stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }
}
