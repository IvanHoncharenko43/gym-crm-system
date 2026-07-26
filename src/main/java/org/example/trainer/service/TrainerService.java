package org.example.trainer.service;

import org.example.exception.InvalidRequestDataException;
import org.example.trainer.controller.response.Trainers;
import org.example.user.controller.dto.UserProfile;
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

    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authenticator){
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
    }

    @Transactional
    public UserProfile create(CreateTrainerRequest request) {
        TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization())
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
        return gymMapper.toUserProfile(savedTrainer.getUser());
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
    public TrainerSummary update(String username, UpdateTrainerRequest request, UserCredentials credentials) {
        authenticator.authenticate(credentials);
        authenticator.authorize(username, credentials);
        TrainerEntity existingTrainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    String message = "Trainer not found";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization())
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
    public void changeActivity(String username, UserCredentials credentials){
        authenticator.authenticate(credentials);
        authenticator.authorize(username, credentials);
        TrainerEntity trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    String message = "Trainer not found";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        trainer.getUser().setIsActive(!trainer.getUser().getIsActive());
        trainerRepository.save(trainer);
        log.info("Activity status changed for a trainer");
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
