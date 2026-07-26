package org.example;

import org.example.training.dto.TrainingType;
import org.example.core.repository.TrainingTypeEntity;
import org.example.user.dto.UserCredentials;
import org.example.user.dto.FullName;
import org.example.user.dto.UserProfile;
import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.repository.TrainingEntity;
import org.example.user.repository.UserEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    public static final Long TRAINEE_ID = 1L;
    public static final String TRAINEE_USERNAME = "John.Doe";
    public static final String TRAINEE_PASSWORD = "122333test";
    public static final Long TRAINER_ID = 1L;
    public static final String TRAINER_USERNAME = "John.Doe1";
    public static final String TRAINER_PASSWORD = "test122333";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";

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
                getTraineeCredentials(), TRAINER_ID, TRAINEE_ID, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
    }

    public static UpdateTraineeRequest getUpdateTraineeRequest(){
        return new UpdateTraineeRequest(
                TRAINEE_ID, getTraineeCredentials(), new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static UpdateTrainerRequest getUpdateTrainerRequest(){
        return new UpdateTrainerRequest(
                TRAINER_ID, getTrainerCredentials(),
                new FullName("John", "Doe"), TrainingType.YOGA
        );
    }

    public static UserEntity getUser(){
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(TRAINEE_USERNAME);
        user.setPassword(TRAINEE_PASSWORD);
        user.setIsActive(true);
        return user;
    }

    public static TraineeEntity getTrainee(){
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(TRAINEE_ID);
        trainee.setUser(getUser());
        trainee.setDateOfBirth(LocalDate.of(2007, 3, 25));
        trainee.setAddress("Home 21 Street");
        return trainee;
    }

    public static TrainerEntity getTrainer(){
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(TRAINER_ID);
        trainer.setUser(getUser());
        trainer.setSpecialization(getTrainingType());
        return trainer;
    }

    public static TrainingEntity getTraining(){
        TrainingEntity training = new TrainingEntity();
        training.setId(1L);
        training.setTrainer(getTrainer());
        training.setTrainee(getTrainee());
        training.setTrainingName("Flexibility");
        training.setTrainingType(getTrainingType());
        training.setTrainingDate(LocalDate.of(2026, 4, 15));
        training.setDurationMinutes(90);
        return training;
    }

    public static TrainingTypeEntity getTrainingType(){
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName(TrainingType.YOGA);
        return trainingType;
    }

    public static TraineeSummary getTraineeSummary(Long id){
        return new TraineeSummary(
                id, new UserProfile(TRAINEE_USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static TraineeSummary getTraineeSummary(String username){
        return new TraineeSummary(
                TRAINEE_ID, new UserProfile(username),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static TrainerSummary getTrainerSummary(Long id){
        return new TrainerSummary(
                id, new UserProfile(TRAINER_USERNAME),
                TrainingType.YOGA
        );
    }

    public static TrainerSummary getTrainerSummary(String username){
        return new TrainerSummary(
                TRAINER_ID, new UserProfile(username),
                TrainingType.YOGA
        );
    }

    public static UserCredentials getTraineeCredentials(){
        return new UserCredentials(
                TRAINEE_USERNAME, TRAINEE_PASSWORD
        );
    }

    public static UserCredentials getTrainerCredentials(){
        return new UserCredentials(
                TRAINER_USERNAME, TRAINER_PASSWORD
        );
    }

    public static Set<String> getExistingUsernamesSet(){
        return new HashSet<>();
    }
}
