package org.example.trainee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraineeRepositoryTest {

    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        traineeRepository = new TraineeRepository();
    }

    @Test
    void findByUsername_ReturnTrainee_UsernameExists() {
        Trainee trainee1 = new Trainee();
        trainee1.setUsername("John.Doe");
        Trainee trainee2 = new Trainee();
        trainee2.setUsername("Jane.Smith");
        traineeRepository.create(trainee1);
        traineeRepository.create(trainee2);
        Optional<Trainee> result = traineeRepository.findByUsername("Jane.Smith");
        assertTrue(result.isPresent(), "Trainee should be found in the storage");
        assertEquals("Jane.Smith", result.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameDoesNotExist() {
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        traineeRepository.create(trainee);
        Optional<Trainee> result = traineeRepository.findByUsername("Unknown.User");
        assertTrue(result.isEmpty(), "Optional should be empty when username is not found");
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameIsNull() {
        Trainee trainee = new Trainee();
        trainee.setUsername("John.Doe");
        traineeRepository.create(trainee);
        Optional<Trainee> result = traineeRepository.findByUsername(null);
        assertTrue(result.isEmpty(), "Optional should be empty when the passed username is null");
    }
}
