package org.example.trainer.service;

import lombok.RequiredArgsConstructor;
import org.example.monitoring.GymCrmMetrics;
import org.example.exception.InvalidRequestDataException;
import org.example.security.controller.dto.LoginDetails;
import org.example.security.service.JwtService;
import org.example.trainer.controller.response.TrainerRegistrationResponse;
import org.example.trainer.controller.response.Trainers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.security.service.OwnershipVerifier;
import org.example.exception.EntityNotFoundException;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
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
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final OwnershipVerifier ownershipVerifier;
    private final GymCrmMetrics gymCrmMetrics;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional
    public TrainerRegistrationResponse create(CreateTrainerRequest request) {
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
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedTrainer.getUser().getUsername());
        String token = jwtService.generateToken(userDetails);
        log.info("Created trainer profile with ID: {}", savedTrainer.getId());
        gymCrmMetrics.incrementTrainerCreation();
        return new TrainerRegistrationResponse(gymMapper.toTrainerSummary(savedTrainer), new LoginDetails(token));
    }

    @Transactional(readOnly = true)
    public TrainerSummary getById(Long id){
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
    public TrainerSummary getByUsername(String username){
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
    public TrainerSummary update(Long id, UpdateTrainerRequest request, UserDetails userDetails) {
        TrainerEntity existingTrainer = trainerRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        ownershipVerifier.verifyOwnershipByUsername(existingTrainer.getUser().getUsername(), userDetails.getUsername());
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
    public void changeActivity(Long id, UserDetails userDetails){
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        ownershipVerifier.verifyOwnershipByUsername(trainer.getUser().getUsername(), userDetails.getUsername());
        trainer.getUser().setIsActive(!trainer.getUser().getIsActive());
        trainerRepository.save(trainer);
        log.info("Activity status changed for a trainer with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Trainers getUnassignedTrainersByTraineeList(String traineeUsername){
        log.debug("Getting unassigned trainers by trainee");
        return new Trainers(
                trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername).stream()
                        .filter(trainer -> trainer.getUser().getIsActive())
                        .map(gymMapper::toTrainerSummary)
                        .toList()
        );
    }
}
