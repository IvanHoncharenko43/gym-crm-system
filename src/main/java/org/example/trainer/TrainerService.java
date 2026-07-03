package org.example.trainer;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
import org.example.shared.GymMapper;
import org.example.shared.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class TrainerService {
    private TrainerRepository trainerRepository;
    private GymMapper gymMapper;

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository){
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setGymMapper(GymMapper gymMapper){
        this.gymMapper = gymMapper;
    }

    public TrainerSummary create(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        Trainer trainer = gymMapper.toTrainerEntity(request);
        Trainer savedTrainer = trainerRepository.create(trainer);
        log.info("Created trainer profile with ID: {}", savedTrainer.getId());
        return gymMapper.toTrainerSummary(savedTrainer);
    }

    public TrainerSummary update(UpdateTrainerRequest request) {
        Objects.requireNonNull(request, "Request body cannot be null");
        Trainer existingTrainer = trainerRepository.getById(request.id())
                .orElseThrow(() -> {
                    log.error("Trainer with ID {} not found", request.id());
                    return new NotFoundException("Trainer with ID " + request.id() + " not found");
                });
        Trainer trainer = gymMapper.toTrainerEntity(request, existingTrainer.getUsername(), existingTrainer.getPassword());
        Trainer updatedTrainer = trainerRepository.update(trainer);
        log.info("Updated trainer profile with ID: {}", updatedTrainer.getId());
        return gymMapper.toTrainerSummary(updatedTrainer);
    }

    public TrainerSummary getById(Long id) {
        Objects.requireNonNull(id, "ID cannot be null");
        log.info("Selecting trainer by ID started");
        return trainerRepository.getById(id)
                .filter(User::isActive)
                .map(gymMapper::toTrainerSummary)
                .orElseThrow(() -> {
                    log.warn("Trainer with ID {} not found", id);
                    return new NotFoundException("Trainer with ID " + id + " not found");
                });
    }
}
