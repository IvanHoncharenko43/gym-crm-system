package org.example;

import org.example.shared.FullName;
import org.example.shared.TrainingType;
import org.example.shared.UserProfile;
import org.example.trainee.CreateTraineeRequest;
import org.example.trainee.TraineeEntity;
import org.example.trainee.TraineeSummary;
import org.example.trainee.UpdateTraineeRequest;
import org.example.trainer.CreateTrainerRequest;
import org.example.trainer.TrainerEntity;
import org.example.trainer.TrainerSummary;
import org.example.trainer.UpdateTrainerRequest;
import org.example.training.CreateTrainingRequest;
import org.example.training.TrainingEntity;

import java.time.LocalDate;

public class TestUtils {

    public static final Long TRAINEE_ID = 1L;
    public static final String TRAINEE_USERNAME = "John.Doe";
    public static final String TRAINEE_PASSWORD = "122333test";
    public static final Long TRAINER_ID = 1L;
    public static final String TRAINER_USERNAME = "John.Doe1";
    public static final String TRAINER_PASSWORD = "test122333";

    public static CreateTraineeRequest getCreateTraineeRequest(){
        return new CreateTraineeRequest(
                new FullName("John", "Doe"), LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static CreateTrainerRequest getCreateTrainerRequest(){
        return new CreateTrainerRequest(
                new FullName("John", "Doe"), TrainingType.YOGA
        );
    }

    public static CreateTrainingRequest getCreateTrainingRequest(){
        return new CreateTrainingRequest(
                TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
    }

    public static UpdateTraineeRequest getUpdateTraineeRequest(){
        return new UpdateTraineeRequest(
                1L, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street", true
        );
    }

    public static UpdateTraineeRequest getUpdateTraineeRequest(Long id){
        return new UpdateTraineeRequest(
                id, new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street", true
        );
    }

    public static UpdateTrainerRequest getUpdateTrainerRequest(){
        return new UpdateTrainerRequest(
                2L, new FullName("John", "Doe"),
                TrainingType.YOGA, true
        );
    }

    public static UpdateTrainerRequest getUpdateTrainerRequest(Long id){
        return new UpdateTrainerRequest(
                id, new FullName("John", "Doe"),
                TrainingType.YOGA, true
        );
    }

    public static TraineeEntity getTrainee(){
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername(TRAINEE_USERNAME);
        trainee.setPassword(TRAINEE_PASSWORD);
        trainee.setActive(true);
        trainee.setDateOfBirth(LocalDate.of(2007, 3, 25));
        trainee.setAddress("Home 21 Street");
        return trainee;
    }

    public static TrainerEntity getTrainer(){
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setUsername(TRAINER_USERNAME);
        trainer.setPassword(TRAINER_PASSWORD);
        trainer.setActive(true);
        trainer.setSpecialization(TrainingType.STRENGTH);
        return trainer;
    }

    public static TrainingEntity getTraining(){
        TrainingEntity training = new TrainingEntity();
        training.setId(1L);
        training.setTrainerId(TRAINER_ID);
        training.setTraineeId(TRAINEE_ID);
        training.setTrainingName("Powerlifting");
        training.setTrainingType(TrainingType.STRENGTH);
        training.setTrainingDate(LocalDate.of(2026, 4, 15));
        training.setDurationMinutes(90);
        return training;
    }

    public static TraineeSummary getTraineeSummary(Long id){
        return new TraineeSummary(
                id, new UserProfile(TRAINEE_USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static TraineeSummary getTraineeSummary(Long id, String username){
        return new TraineeSummary(
                id, new UserProfile(username),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static TrainerSummary getTrainerSummary(Long id){
        return new TrainerSummary(
                id, new UserProfile(TRAINER_USERNAME),
                TrainingType.YOGA
        );
    }
    public static TrainerSummary getTrainerSummary(Long id, String username){
        return new TrainerSummary(
                id, new UserProfile(username),
                TrainingType.YOGA
        );
    }
}
