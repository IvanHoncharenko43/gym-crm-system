package org.example.training.service;

import org.example.exception.InvalidRequestDataException;
import org.example.training.dto.request.Trainings;
import org.example.user.dto.UserCredentials;
import org.springframework.transaction.annotation.Transactional;
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
    public TrainingSummary create(CreateTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TraineeEntity trainee = traineeRepository.findById(request.traineeId())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found or is inactive", request.traineeId());
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });

        TrainerEntity trainer = trainerRepository.findById(request.trainerId())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found or is inactive", request.trainerId());
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });
        TrainingEntity training = gymMapper.toTraining(request, trainee, trainer);
        TrainingEntity savedTraining = trainingRepository.save(training);
        log.info("Created training with ID: {}", savedTraining.getId());
        return gymMapper.toTrainingSummary(savedTraining, trainee, trainer);
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
    public TrainingSummary getById(GetTrainingRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TrainingEntity training = trainingRepository.findById(request.id())
                .orElseThrow(() -> {
                    String message = String.format("Training with ID %s not found", request.id());
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        TraineeEntity trainee = traineeRepository.findById(training.getTrainee().getId()).get();
        TrainerEntity trainer = trainerRepository.findById(training.getTrainer().getId()).get();
        log.info("Selected training by ID: {}", request.id());
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    @Transactional(readOnly = true)
    public List<TrainingSummary> getTraineeTrainingList(GetTraineeTrainingsRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        return trainingRepository.findTraineeTrainingsByCriteria(
                request.credentials().username(), request.fromDate(), request.toDate(),
                request.trainerName(), request.trainingType().name()).stream()
                .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                .toList();
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
    public List<TrainingSummary> getTrainerTrainingList(GetTrainerTrainingsRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        return trainingRepository.findTrainerTrainingsByCriteria(
                request.credentials().username(), request.fromDate(),
                        request.toDate(), request.traineeName()).stream()
                .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                .toList();
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
