package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.example.config.AppConfig;
import org.example.core.dto.ChangeActivityRequest;
import org.example.core.dto.ChangePasswordRequest;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.dto.GetTraineeTrainingsRequest;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainee.service.TraineeService;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.dto.UnassignedTrainersRequest;
import org.example.trainer.repository.TrainerRepository;
import org.example.trainer.service.TrainerService;
import org.example.trainer.dto.TrainerSummary;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.dto.TrainingType;
import org.example.training.service.TrainingService;
import org.example.training.dto.TrainingSummary;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class App
{
    public static void main( String[] args )
    {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);
            TraineeRepository traineeRepository = context.getBean(TraineeRepository.class);
            TrainerRepository trainerRepository = context.getBean(TrainerRepository.class);

            PlatformTransactionManager txManager = context.getBean(PlatformTransactionManager.class);
            TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
            transactionTemplate.setReadOnly(true);

            log.info("---- START ----");

            log.info("--- 1. Testing CREATE ---");
            CreateTraineeRequest traineeCreateRequest = new CreateTraineeRequest(
                    new FullName("John", "Doe"), LocalDate.of(1995, 5, 15), "Home 21 Street");
            TraineeSummary createdTrainee = traineeService.create(traineeCreateRequest);
            String traineeUsername = createdTrainee.profile().username();

            String traineePassword = transactionTemplate.execute(status ->
                    traineeRepository.findByUsername(traineeUsername)
                            .orElseThrow(() -> new RuntimeException("Trainee not found after creation"))
                            .getUser()
                            .getPassword());

            UserCredentials traineeCredentials = new UserCredentials(traineeUsername, traineePassword);
            log.info("Created Trainee: {}, Fetched Password from DB: {}", traineeUsername, traineePassword);

            CreateTrainerRequest trainerCreateRequest = new CreateTrainerRequest(
                    new FullName("Jane", "Smith"), TrainingType.YOGA);
            TrainerSummary createdTrainer = trainerService.create(trainerCreateRequest);
            String trainerUsername = createdTrainer.profile().username();

            String trainerPassword = transactionTemplate.execute(status ->
                    trainerRepository.findByUsername(trainerUsername)
                            .orElseThrow(() -> new RuntimeException("Trainer not found after creation"))
                            .getUser()
                            .getPassword());

            UserCredentials trainerCredentials = new UserCredentials(trainerUsername, trainerPassword);
            log.info("Created Trainer: {}, Fetched Password from DB: {}", trainerUsername, trainerPassword);

            log.info("--- 2. Testing AUTHENTICATION FAILURE ---");
            try {
                UserCredentials wrongCreds = new UserCredentials(traineeUsername, "wrongPassword");
                traineeService.getByUsername(wrongCreds);
            } catch (Exception e) {
                log.error("Expected Authentication Exception occurred: {}", e.getMessage());
            }

            log.info("--- 3. Testing SELECT BY USERNAME ---");
            TraineeSummary fetchedTrainee = traineeService.getByUsername(traineeCredentials);
            log.info("Fetched Trainee Address: {}", fetchedTrainee.address());

            log.info("--- 4. Testing CHANGE PASSWORD ---");
            String newPassword = "newSecurePassword123";
            traineeService.changePassword(new ChangePasswordRequest(traineeCredentials, newPassword));
            traineeCredentials = new UserCredentials(traineeUsername, newPassword);
            log.info("Trainee password changed successfully");

            log.info("--- 5. Testing CHANGE ACTIVITY STATUS ---");
            traineeService.changeActivity(new ChangeActivityRequest(traineeCredentials));
            log.info("Trainee activity status set to false");
            traineeService.changeActivity(new ChangeActivityRequest(traineeCredentials));
            log.info("Trainee activity status set to true");

            log.info("--- 6. Testing UPDATE PROFILE ---");
            UpdateTraineeRequest traineeUpdateRequest = new UpdateTraineeRequest(
                    createdTrainee.id(), traineeCredentials,
                    new FullName("John", "Doe"), LocalDate.of(1995, 5, 15), "Updated Address 99"
            );
            TraineeSummary updatedTrainee = traineeService.update(traineeUpdateRequest);
            log.info("Trainee new address: {}", updatedTrainee.address());

            log.info("--- 7. Testing UNASSIGNED TRAINERS LIST ---");
            List<TrainerSummary> unassignedTrainers = trainerService.getUnassignedTrainersByTraineeList(
                    new UnassignedTrainersRequest(trainerCredentials, "John.Doe")
            );
            log.info("Found {} unassigned trainers", unassignedTrainers.size());

            log.info("--- 8. Testing ADD TRAINING ---");
            CreateTrainingRequest trainingCreateRequest = new CreateTrainingRequest(
                    traineeCredentials, createdTrainer.id(), createdTrainee.id(), "Morning Yoga", LocalDate.now(), 60
            );
            trainingService.create(trainingCreateRequest);
            log.info("Successfully created training session");

            log.info("--- 9. Testing GET TRAININGS BY CRITERIA ---");
            GetTraineeTrainingsRequest criteriaRequest = new GetTraineeTrainingsRequest(
                    traineeCredentials, LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1), trainerUsername, TrainingType.YOGA
            );
            List<TrainingSummary> trainings = trainingService.getTraineeTrainingList(criteriaRequest);
            log.info("Found {} trainings matching the criteria", trainings.size());

            log.info("--- 10. Testing DELETE BY USERNAME ---");
            traineeService.deleteByUsername(traineeCredentials);
            log.info("Trainee {} successfully deleted", traineeUsername);

            log.info("---- FINISH ----");
        } catch (Exception e) {
            log.error("Application execution failed: {}", e.getMessage(), e);
        }
    }
}
