package org.example.trainer.service;

import org.example.exception.InvalidRequestDataException;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.core.service.AuthenticationComponent;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.exception.InvalidPasswordException;
import org.example.exception.EntityNotFoundException;
import org.example.trainer.dto.UnassignedTrainersRequest;
import org.example.core.repository.TrainingTypeEntity;
import org.example.core.repository.TrainingTypeRepository;
import org.example.user.dto.UserCredentials;
import org.example.core.service.GymMapper;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final AuthenticationComponent authenticator;
    private final TransactionTemplate transactionTemplate;
    private final ConcurrentHashMap<String, ReentrantLock> baseNameLocks = new ConcurrentHashMap<>();

    public TrainerService(TrainerRepository trainerRepository, TrainingTypeRepository trainingTypeRepository, UserRepository userRepository,
                          GymMapper gymMapper, AuthenticationComponent authenticator, TransactionTemplate transactionTemplate){
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.gymMapper = gymMapper;
        this.authenticator = authenticator;
        this.transactionTemplate = transactionTemplate;
    }

    public TrainerSummary create(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        ReentrantLock lock = baseNameLocks.computeIfAbsent(baseName, l -> new ReentrantLock());
        lock.lock();
        try{
            return transactionTemplate.execute(status -> {
                TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization())
                        .orElseThrow(() -> {
                            String message = String.format("Training type %s not found", request.specialization().name());
                            return createInvalidRequestDataException(message);
                        });
                Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseName(baseName));
                TrainerEntity trainer = gymMapper.toTrainerEntity(request, trainingType, existingUsernames);
                TrainerEntity savedTrainer = trainerRepository.save(trainer);
                log.info("Created trainer profile with ID: {}", savedTrainer.getId());
                return gymMapper.toTrainerSummary(savedTrainer);
            });
        } finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public TrainerSummary getByUsername(UserCredentials credentials){
        Objects.requireNonNull(credentials, "Credentials cannot be null");
        authenticator.authenticate(credentials);
        log.info("Selecting trainer by username started");
        String username = credentials.username();
        return trainerRepository.findByUsername(username)
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    String message = "Trainer not found or is inactive";
                    return createEntityNotFoundException(message);
                });
    }

    @Transactional
    public TrainerSummary update(UpdateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TrainerEntity existingTrainer = trainerRepository.findById(request.id())
                .orElseThrow(() -> {
                    String message = String.format("Trainer with ID %s not found", request.id());
                    return createEntityNotFoundException(message);
                });
        TrainingTypeEntity trainingType = trainingTypeRepository.findByName(request.specialization())
                .orElseThrow(() -> {
                    String message = String.format("Training type %s not found", request.specialization().name());
                    return createInvalidRequestDataException(message);
                });
        TrainerEntity trainer = gymMapper.toTrainerEntity(request, existingTrainer, trainingType);
        TrainerEntity updatedTrainer = trainerRepository.save(trainer);
        log.info("Updated trainer profile with ID: {}", updatedTrainer.getId());
        return gymMapper.toTrainerSummary(updatedTrainer);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TrainerEntity trainer = trainerRepository.findByUsername(request.credentials().username())
                .filter(t -> t.getUser().getIsActive())
                .orElseThrow(() -> {
                    String message = "Trainer not found or is inactive";
                    return createEntityNotFoundException(message);
                });
        if(request.newPassword().length() < 10){
            throw new InvalidPasswordException("Password should be at least 10 characters");
        }
        trainer.getUser().setPassword(request.newPassword());
        trainerRepository.save(trainer);
    }

    @Transactional
    public void changeActivity(ChangeActivityRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        TrainerEntity trainer = trainerRepository.findByUsername(request.credentials().username())
                .orElseThrow(() -> {
                    String message = "Trainer with ID %s not found";
                    return createEntityNotFoundException(message);
                });
        trainer.getUser().setIsActive(!trainer.getUser().getIsActive());
        trainerRepository.save(trainer);
        log.info("Activity status changed for a trainer");
    }

    @Transactional(readOnly = true)
    public List<TrainerSummary> getUnassignedTrainersByTraineeList(UnassignedTrainersRequest request){
        Objects.requireNonNull(request, "Request body cannot be null");
        authenticator.authenticate(request.credentials());
        return trainerRepository.findUnassignedTrainersByTraineeUsername(request.traineeUsername()).stream()
                .filter(trainer -> trainer.getUser().getIsActive())
                .map(gymMapper::toTrainerSummary)
                .toList();
    }

    private EntityNotFoundException createEntityNotFoundException(String message){
        log.warn(message);
        return new EntityNotFoundException(message);
    }

    private InvalidRequestDataException createInvalidRequestDataException(String message){
        log.warn(message);
        return new InvalidRequestDataException(message);
    }
}
