package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.shared.UsernameGenerator;
import org.example.trainee.TraineeRepository;
import org.example.trainer.TrainerRepository;
import org.example.training.TrainingRepository;
import org.example.shared.Identifiable;
import org.example.trainee.TraineeEntity;
import org.example.trainer.TrainerEntity;
import org.example.training.TrainingEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
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
        switch (bean) {
            case TraineeRepository traineeRepository -> {
                Map<Long, TraineeEntity> initialData = loadDataAndMapToStorage("trainees", TraineeEntity[].class);
                traineeRepository.initStorage(initialData);
            }
            case TrainerRepository trainerRepository -> {
                Map<Long, TrainerEntity> initialData = loadDataAndMapToStorage("trainers", TrainerEntity[].class);
                trainerRepository.initStorage(initialData);
            }
            case TrainingRepository trainingRepository -> {
                Map<Long, TrainingEntity> initialData = loadDataAndMapToStorage("trainings", TrainingEntity[].class);
                trainingRepository.initStorage(initialData);
            }
            case UsernameGenerator usernameGenerator -> {
                List<String> existingUsernames = extractUsernames();
                usernameGenerator.initData(existingUsernames);
            }
            default -> {
            }
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
                            Function.identity()
                    ));
        } catch (Exception e) {
            log.error("Failed to map {} data. Reason: {}", nodeName, e.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<String> extractUsernames() {
        Map<Long, TraineeEntity> trainees = loadDataAndMapToStorage("trainees", TraineeEntity[].class);
        List<String> allUsernames = new ArrayList<>(trainees.values().stream()
                .map(TraineeEntity::getUsername)
                .filter(Objects::nonNull)
                .toList());
        Map<Long, TrainerEntity> trainers = loadDataAndMapToStorage("trainers", TrainerEntity[].class);
        allUsernames.addAll(trainers.values().stream()
                .map(TrainerEntity::getUsername)
                .filter(Objects::nonNull)
                .toList());

        log.info("Extracted {} existing usernames for UsernameGenerator initialization", allUsernames.size());
        return allUsernames;
    }
}
