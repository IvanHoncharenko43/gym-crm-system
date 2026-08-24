package org.example.crm.trainee.service;

import lombok.RequiredArgsConstructor;
import org.example.crm.core.dto.ActionType;
import org.example.crm.monitoring.GymCrmMetrics;
import org.example.crm.trainer.controller.request.TrainerWorkloadRequest;
import org.example.crm.trainer.controller.response.Trainers;
import org.example.crm.trainer.service.TrainerWorkloadAdapter;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.exception.EntityNotFoundException;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.controller.request.UpdateTraineeRequest;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.controller.request.UpdateTraineeTrainersRequest;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.core.service.GymMapper;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final GymMapper gymMapper;
    private final GymCrmMetrics gymCrmMetrics;
    private final TrainerWorkloadAdapter trainerWorkloadAdapter;

    @Transactional
    public TraineeSummary create(CreateTraineeRequest request) {
        String baseName = request.fullName().firstName() + "." + request.fullName().lastName();
        Set<String> existingUsernames = new HashSet<>(userRepository.findUsernamesByBaseNameForUpdate(baseName));
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingUsernames);
        TraineeEntity savedTrainee = traineeRepository.save(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        gymCrmMetrics.incrementTraineeCreation();
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    @Transactional(readOnly = true)
    public TraineeSummary getById(Long id){
        return traineeRepository.findById(id)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found or is inactive", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional(readOnly = true)
    public TraineeSummary getByUsername(String username){
        return traineeRepository.findByUsername(username)
                .filter(trainee -> trainee.getUser().getIsActive())
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    String message = "Trainee not found or is inactive";
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
    }

    @Transactional
    public TraineeSummary update(Long id, UpdateTraineeRequest request) {
        TraineeEntity existingTrainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee);
        TraineeEntity updatedTrainee = traineeRepository.save(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    @Transactional
    public void deleteByUsername(String username){
        Optional<TraineeEntity> existingTrainee = traineeRepository.findByUsername(username);
        if(existingTrainee.isPresent()) {
            List<TrainingEntity> trainings = trainingRepository.findAllByTraineeUserUsername(username);
            for (TrainingEntity training : trainings){
                TrainerWorkloadRequest request = gymMapper.toTrainerWorkloadRequest(training.getTrainer(), training, ActionType.DELETE);
                trainerWorkloadAdapter.updateTrainerWorkload(request);
            }
            traineeRepository.deleteByUserUsername(existingTrainee.get().getUser().getUsername());
            log.info("Deleted by username a trainee profile with ID: {}", existingTrainee.get().getId());
        }
    }

    @Transactional
    public void changeActivity(Long id){
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        trainee.getUser().setIsActive(!trainee.getUser().getIsActive());
        traineeRepository.save(trainee);
        log.info("Activity status changed for a trainee with ID: {}", id);
    }

    @Transactional
    public Trainers updateTrainersList(Long id, UpdateTraineeTrainersRequest request){
        TraineeEntity trainee = traineeRepository.findById(id)
                .orElseThrow(() -> {
                    String message = String.format("Trainee with ID %s not found", id);
                    log.warn(message);
                    return new EntityNotFoundException(message);
                });
        Set<TrainerEntity> newTrainers = new HashSet<>(trainerRepository.findByUsernames(request.trainerUsernames()));
        trainee.getTrainers().stream()
                .filter(oldTrainer -> !newTrainers.contains(oldTrainer))
                .forEach(oldTrainer -> oldTrainer.getTrainees().remove(trainee));
        newTrainers.forEach(newTrainer -> newTrainer.getTrainees().add(trainee));
        trainee.getTrainers().addAll(newTrainers);
        traineeRepository.save(trainee);
        gymCrmMetrics.incrementTrainerAssignment();
        log.info("Updated trainers for a trainee ID: {}", id);
        return new Trainers(
                newTrainers.stream()
                        .map(gymMapper::toTrainerSummary)
                        .toList()
        );
    }
}
