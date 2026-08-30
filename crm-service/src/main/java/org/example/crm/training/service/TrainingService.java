package org.example.crm.training.service;

import lombok.RequiredArgsConstructor;
import org.example.crm.core.dto.ActionType;
import org.example.crm.monitoring.GymCrmMetrics;
import org.example.crm.exception.InvalidRequestDataException;
import org.example.crm.trainer.client.dto.request.TrainerWorkloadRequest;
import org.example.crm.trainer.service.TrainerWorkloadGateway;
import org.example.crm.training.controller.response.Trainings;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.trainee.controller.request.GetTraineeTrainingsRequest;
import org.example.crm.trainer.controller.request.GetTrainerTrainingsRequest;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.core.service.GymMapper;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final GymMapper gymMapper;
    private final GymCrmMetrics gymCrmMetrics;
    private final TrainerWorkloadGateway trainerWorkloadGateway;

    @Transactional
    public TrainingSummary create(CreateTrainingRequest request) {
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
        gymCrmMetrics.incrementTrainingCreated(training.getTrainingType().getTrainingTypeName().name());
        TrainerWorkloadRequest trainerWorkloadRequest = gymMapper.toTrainerWorkloadRequest(trainer, training, ActionType.ADD);
        trainerWorkloadGateway.updateTrainerWorkload(trainerWorkloadRequest);
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    @Transactional
    public void cancel(Long id) {
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Training with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        if (training.getTrainingDate().isBefore(LocalDate.now())) {
            String message = String.format("Cannot cancel training with ID %s as training date has already passed", id);
            log.warn(message);
            throw new InvalidRequestDataException(message);
        }
        TrainerWorkloadRequest trainerWorkloadRequest = gymMapper.toTrainerWorkloadRequest(training.getTrainer(), training, ActionType.DELETE);
        trainingRepository.delete(training);
        log.info("Cancelled training with ID: {}", id);
        trainerWorkloadGateway.updateTrainerWorkload(trainerWorkloadRequest);
    }

    @Transactional(readOnly = true)
    public TrainingSummary getById(Long id) {
        TrainingEntity training = trainingRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Training with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        TraineeEntity trainee = traineeRepository.findById(training.getTrainee().getId()).get();
        TrainerEntity trainer = trainerRepository.findById(training.getTrainer().getId()).get();
        log.debug("Selected training by ID: {}", id);
        return gymMapper.toTrainingSummary(training, trainee, trainer);
    }

    @Transactional(readOnly = true)
    public Trainings getTraineeTrainingList(GetTraineeTrainingsRequest request){
        log.debug("Getting trainee trainings");
        return new Trainings(
                trainingRepository.findTraineeTrainings(request.username(), request.fromDate(), request.toDate(),
                                request.trainerName(), request.trainingType().name())
                        .stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Trainings getTrainerTrainingList(GetTrainerTrainingsRequest request){
        log.debug("Getting trainer trainings");
        return new Trainings(
                trainingRepository.findTrainerTrainings(request.username(), request.fromDate(),
                                request.toDate(), request.traineeName())
                        .stream()
                        .map(training -> gymMapper.toTrainingSummary(training, training.getTrainee(), training.getTrainer()))
                        .toList()
        );
    }
}
