package org.example.trainee;

import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraineeRepositoryTest {

    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        traineeRepository = new TraineeRepository();
        traineeRepository.initStorage(new ConcurrentHashMap<>());
    }

    @Test
    void findByUsername_ReturnTrainee_UsernameExists() {
        TraineeEntity trainee1 = new TraineeEntity();
        trainee1.setUsername("John.Doe");
        TraineeEntity trainee2 = new TraineeEntity();
        trainee2.setUsername("Jane.Smith");
        traineeRepository.create(trainee1);
        traineeRepository.create(trainee2);
        Optional<TraineeEntity> result = traineeRepository.findByUsername("Jane.Smith");
        assertTrue(result.isPresent(), "Trainee should be found in the storage");
        assertEquals("Jane.Smith", result.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameDoesNotExist() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUsername("John.Doe");
        traineeRepository.create(trainee);
        Optional<TraineeEntity> result = traineeRepository.findByUsername("Unknown.User");
        assertTrue(result.isEmpty(), "Optional should be empty when username is not found");
    }
}
