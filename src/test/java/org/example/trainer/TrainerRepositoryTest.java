package org.example.trainer;

import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainerRepositoryTest {

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        trainerRepository = new TrainerRepository();
        trainerRepository.initStorage(new ConcurrentHashMap<>());
    }

    @Test
    void findByUsername_ReturnTrainer_UsernameExists() {
        TrainerEntity trainer1 = new TrainerEntity();
        trainer1.setUsername("John.Doe");
        TrainerEntity trainer2 = new TrainerEntity();
        trainer2.setUsername("Jane.Smith");
        trainerRepository.create(trainer1);
        trainerRepository.create(trainer2);
        Optional<TrainerEntity> result = trainerRepository.findByUsername("John.Doe");
        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUsername());
    }

    @Test
    void findByUsername_ReturnEmptyOptional_UsernameDoesNotExist() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUsername("John.Doe");
        trainerRepository.create(trainer);
        Optional<TrainerEntity> result = trainerRepository.findByUsername("Unknown.Trainer");
        assertTrue(result.isEmpty());
    }
}
