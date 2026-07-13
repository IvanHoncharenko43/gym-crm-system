package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.config.AppConfig;
import org.example.user.dto.FullName;
import org.example.training.enums.TrainingType;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.service.TraineeService;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.service.TrainerService;
import org.example.trainer.dto.TrainerSummary;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.service.TrainingService;
import org.example.training.dto.TrainingSummary;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            log.info("---- STARTING ----");
            log.info("--- 1. Testing CREATE ---");
            CreateTraineeRequest traineeCreateRequest = new CreateTraineeRequest(new FullName("John", "Doe"),
                    LocalDate.of(1995, 5, 15), "Home 21 Street");
            TraineeSummary createdTrainee = traineeService.create(traineeCreateRequest);
            Long traineeId = createdTrainee.id();
            CreateTrainerRequest trainerCreateRequest = new CreateTrainerRequest(new FullName("Jane", "Smith"),
                    TrainingType.YOGA);
            TrainerSummary createdTrainer = trainerService.create(trainerCreateRequest);
            Long trainerId = createdTrainer.id();

            log.info("--- 2. Testing SELECT ---");
            TraineeSummary fetchedTrainee = traineeService.getById(traineeId);
            log.info("Fetched Trainee: {} {}", fetchedTrainee.id(), fetchedTrainee.profile().username());
            TrainerSummary fetchedTrainer = trainerService.getById(trainerId);
            log.info("Fetched Trainer Specialization: {}", fetchedTrainer.specialization());

            log.info("--- 3. Testing UPDATE ---");
            UpdateTraineeRequest traineeUpdateRequest = new UpdateTraineeRequest(
                    fetchedTrainee.id(), new FullName("John", "Doe"),
                    LocalDate.of(1995, 5, 15), "Home 23 Street", true
            );
            TraineeSummary updatedTrainee = traineeService.update(traineeUpdateRequest);
            log.info("Trainee new address: {}", updatedTrainee.address());

            log.info("--- 4. Testing CREATE TRAINING ---");
            CreateTrainingRequest trainingCreateRequest = new CreateTrainingRequest(
                    trainerId, traineeId, "Morning Yoga Flow", LocalDate.now(), 60
            );
            TrainingSummary createdTraining = trainingService.create(trainingCreateRequest);
            log.info("Successfully created training: {}", createdTraining.trainingName());

            log.info("--- 5. Testing DELETE ---");
            traineeService.deleteById(traineeId);
            log.info("Trainee with ID {} successfully deleted.", traineeId);

            log.info("--- 6. Testing EXCEPTION ---");
            try {
                traineeService.getById(traineeId);
            } catch (Exception e) {
                log.error("Expected exception occurred: {}", e.getMessage());
            }
            log.info("---- FINISHING ----");
        } catch (Exception e) {
            log.error("Application execution failed: {}", e.getMessage(), e);
        }
    }
}
