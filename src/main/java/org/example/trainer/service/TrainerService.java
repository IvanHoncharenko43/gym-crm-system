package org.example.trainer.service;

import org.example.monitoring.TransactionalMetricService;
import org.example.exception.InvalidRequestDataException;
import org.example.trainer.controller.response.Trainers;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.exception.EntityNotFoundException;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.user.controller.dto.UserCredentials;
import org.example.core.service.GymMapper;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;
    private final TransactionalMetricService transactionalMetricService;

    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authenticator, TransactionalMetricService transactionalMetricService){
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
        this.transactionalMetricService = transactionalMetricService;
    }

    @Transactional
    public TrainerSummary create(CreateTrainerRequest request) {
        TrainingTypeEntity trainingType = trainingTypeRepository.findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> {
                    String message = String.format("Training type %s not found", request.specialization().name());
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseNameForUpdate(baseName));
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, trainingType, existingUsernames);
        TrainerEntity savedTrainer = trainerRepository.save(trainer);
        log.info("Created trainer profile with ID: {}", savedTrainer.getId());
        transactionalMetricService.incrementOnCommit("gym_users_created_total", "role", "TRAINER");
        return gymMapper.toTrainerSummary(savedTrainer);
    }

    @Transactional(readOnly = true)
    public TrainerSummary getById(Long id, UserCredentials credentials){
        authenticator.authenticate(credentials);
        log.info("Selecting trainer by ID started");
        return trainerRepository.findById(id)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found or is inactive", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional(readOnly = true)
    public TrainerSummary getByUsername(String username, UserCredentials credentials){
        authenticator.authenticate(credentials);
        log.info("Selecting trainer by username started");
        return trainerRepository.findByUsername(username)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    String message = "Trainer not found or is inactive";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional
    public TrainerSummary update(Long id, UpdateTrainerRequest request, UserCredentials credentials) {
        authenticator.authenticate(credentials);
        TrainerEntity existingTrainer = trainerRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        authenticator.authorize(existingTrainer.getUser().getUsername(), credentials);
        TrainingTypeEntity trainingType = trainingTypeRepository.findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> {
                    String message = String.format("Training type %s not found", request.specialization().name());
                    log.warn(message);
                    return new InvalidRequestDataException(message);
                });
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, existingTrainer, trainingType);
        TrainerEntity updatedTrainer = trainerRepository.save(trainer);
        log.info("Updated trainer profile with ID: {}", updatedTrainer.getId());
        return gymMapper.toTrainerSummary(updatedTrainer);
    }

    @Transactional
    public void changeActivity(Long id, UserCredentials credentials){
        authenticator.authenticate(credentials);
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        authenticator.authorize(trainer.getUser().getUsername(), credentials);
        trainer.getUser().setIsActive(!trainer.getUser().getIsActive());
        trainerRepository.save(trainer);
        log.info(String.format("Activity status changed for a trainer with ID %s", id));
    }

    @Transactional(readOnly = true)
    public Trainers getUnassignedTrainersByTraineeList(String traineeUsername, UserCredentials credentials){
        authenticator.authenticate(credentials);
        return new Trainers(
                trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername).stream()
                        .filter(trainer -> trainer.getUser().getIsActive())
                        .map(gymMapper::toTrainerSummary)
                        .toList()
        );
    }
}
