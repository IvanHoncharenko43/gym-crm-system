package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerRepository;
import org.example.training.TrainingRepository;
import org.example.shared.Identifiable;
import org.example.trainee.Trainee;
import org.example.trainer.Trainer;
import org.example.training.Training;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataInitializationProcessor implements BeanPostProcessor {

    @Value("${data.file.path}")
    private String filePath;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonNode rootNode;

    @PostConstruct
    public void init() {
        log.info("Initializing DataInitializationProcessor. Attempting to load file: {}", filePath);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                log.warn("Data file not found in classpath at path: {}", filePath);
                return;
            }
            rootNode = objectMapper.readTree(inputStream);
            log.info("Successfully loaded and parsed initial data file.");
        } catch (Exception e) {
            log.error("Failed to parse data file {}. Reason: {}", filePath, e.getMessage());
        }
    }

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (rootNode == null) {
            return bean;
        }
        if (bean instanceof TraineeRepository traineeRepository) {
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

    private <T extends Identifiable> Map<Long, T> loadDataAndMapToStorage(String nodeName, Class<T[]> clazz) {
        JsonNode arrayNode = rootNode.get(nodeName);
        if (arrayNode == null || arrayNode.isNull()) {
            log.info("No data found for node '{}' in file", nodeName);
            return Collections.emptyMap();
        }

        try {
            T[] items = objectMapper.treeToValue(arrayNode, clazz);
            return Arrays.stream(items)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toConcurrentMap(
                            T::getId,
                            item -> item,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.error("Failed to map {} data. Reason: {}", nodeName, e.getMessage());
            return Collections.emptyMap();
        }
    }
}
