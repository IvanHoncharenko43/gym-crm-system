package org.example.config;

import org.example.domain.Trainee;
import org.example.domain.Trainer;
import org.example.domain.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StorageConfig {

    @Bean
    public Map<Long, Trainer> getTrainerStorage(){
        return new HashMap<>();
    }

    @Bean
    public Map<Long, Trainee> getTraineeStorage(){
        return new HashMap<>();
    }

    @Bean
    public Map<Long, Training> getTrainingStorage(){
        return new HashMap<>();
    }
}
