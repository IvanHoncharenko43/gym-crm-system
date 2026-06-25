package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dao.TrainingRepository;
import org.example.domain.Identifiable;
import org.example.trainee.Trainee;
import org.example.trainer.Trainer;
import org.example.training.Training;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataInitializationProcessor implements BeanPostProcessor {

    private String filePath;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof TraineeRepository traineeRepository){
            Map<Long, Trainee> initialData = loadDataAndMapToStorage("trainees", Trainee[].class);
            traineeRepository.initStorage(initialData);
        }
        else if (bean instanceof TrainerRepository trainerRepository) {
            Map<Long, Trainer> initialData = loadDataAndMapToStorage("trainers", Trainer[].class);
            trainerRepository.initStorage(initialData);
        }
        else if (bean instanceof TrainingRepository trainingRepository) {
            Map<Long, Training> initialData = loadDataAndMapToStorage("trainings", Training[].class);
            trainingRepository.initStorage(initialData);
        }
        return bean;
    }

    private <T extends Identifiable> Map<Long, T> loadDataAndMapToStorage(String nodeName, Class<T[]> clazz){
        try{
            Path path = Path.of(filePath);
            if(Files.notExists(path)){
                log.warn("Data file not found at path: " + filePath);
                return Collections.emptyMap();
            }
            JsonNode rootNode = objectMapper.readTree(Files.newInputStream(path));
            JsonNode arrayNode = rootNode.get(nodeName);
            if (arrayNode == null || arrayNode.isNull()) {
                log.info("No data found for {} in file", nodeName);
                return Collections.emptyMap();
            }
            T[] items = objectMapper.treeToValue(arrayNode, clazz);
            return Arrays.stream(items)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            T::getId,
                            item -> item,
                            (existing, replacement) -> existing
                    ));
        } catch (IOException e) {
            log.error("Failed to load {} from {}. Reason: {}", nodeName, filePath, e.getMessage());
            return Collections.emptyMap();
        }
    }
}
