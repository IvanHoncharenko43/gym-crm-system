package org.example.trainee.service;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
import org.example.utils.GymMapper;
import org.example.user.repository.UserEntity;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TraineeService {
    private TraineeRepository traineeRepository;
    private GymMapper gymMapper;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper) {
        this.gymMapper = gymMapper;
    }

    public TraineeSummary create(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TraineeEntity trainee = gymMapper.toTraineeEntity(request);
        TraineeEntity savedTrainee = traineeRepository.create(trainee);
        log.info("Created trainee profile with ID: {}", savedTrainee.getId());
        return gymMapper.toTraineeSummary(savedTrainee);
    }

    public TraineeSummary update(UpdateTraineeRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        TraineeEntity existingTrainee = traineeRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainee with ID {} not found", request.id());
                    return new NotFoundException("Trainee with ID " + request.id() + " not found");
                });
        TraineeEntity trainee = gymMapper.toTraineeEntity(request, existingTrainee.getUsername(), existingTrainee.getPassword());
        TraineeEntity updatedTrainee = traineeRepository.update(trainee);
        log.info("Updated trainee profile with ID: {}", updatedTrainee.getId());
        return gymMapper.toTraineeSummary(updatedTrainee);
    }

    public TraineeSummary getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainee by ID started");
        return traineeRepository.getById(id)
                .filter(UserEntity::isActive)
                .map(gymMapper::toTraineeSummary)
                .orElseThrow(() -> {
                    log.warn("Trainee with ID {} not found", id);
                    return new NotFoundException("Trainee with ID " + id + " not found");
                });
    }

    public void deleteById(Long id) {
        traineeRepository.deleteById(id);
        log.info("Deleted trainee profile with ID: {}", id);
    }
}
