package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.config.AppConfig;
import org.example.shared.TrainingType;
import org.example.trainee.CreateTraineeRequest;
import org.example.trainee.TraineeService;
import org.example.trainee.UpdateTraineeRequest;
import org.example.trainer.CreateTrainerRequest;
import org.example.trainer.TrainerService;
import org.example.training.CreateTrainingRequest;
import org.example.training.TrainingService;
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
            var traineeDto = new CreateTraineeRequest("John", "Doe", LocalDate.of(1995, 5, 15), "123 Street");
            var createdTrainee = traineeService.create(traineeDto);
            String traineeUsername = createdTrainee.profile().username();
            var trainerDto = new CreateTrainerRequest("Jane", "Smith", TrainingType.YOGA);
            var createdTrainer = trainerService.create(trainerDto);
            String trainerUsername = createdTrainer.profile().username();

            log.info("--- 2. Testing SELECT ---");
            var fetchedTrainee = traineeService.getById(createdTrainee.id());
            log.info("Fetched Trainee: {} {}", fetchedTrainee.profile().firstName(), fetchedTrainee.profile().lastName());
            var fetchedTrainer = trainerService.getById(createdTrainer.id());
            log.info("Fetched Trainer Specialization: {}", fetchedTrainer.specialization());

            log.info("--- 3. Testing UPDATE ---");
            var traineeUpdateDto = new UpdateTraineeRequest(
                    fetchedTrainee.id(), "John", "Doe", LocalDate.of(1995, 5, 15), "456 New Address St", true
            );
            var updatedTrainee = traineeService.update(traineeUpdateDto);
            log.info("Trainee new address: {}", updatedTrainee.address());

            log.info("--- 4. Testing CREATE TRAINING ---");
            var trainingDto = new CreateTrainingRequest(
                    trainerUsername, traineeUsername, "Morning Yoga Flow", LocalDate.now(), 60
            );
            var createdTraining = trainingService.create(trainingDto);
            log.info("Successfully created training: {}", createdTraining.trainingName());

            log.info("--- 5. Testing DELETE ---");
            traineeService.deleteById(createdTrainee.id());
            log.info("Trainee with ID {} successfully deleted.", createdTrainee.id());

            log.info("--- 6. Testing EXCEPTION ---");
            try {
                traineeService.getById(createdTrainee.id());
            } catch (Exception e) {
                log.error("Expected exception occurred: {}", e.getMessage());
            }
            log.info("---- FINISHING ----");
        } catch (Exception e) {
            log.error("Application execution failed: {}", e.getMessage(), e);
        }
    }
}
