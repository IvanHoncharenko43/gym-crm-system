package org.example;

import org.example.training.controller.response.TrainingSummary;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.user.controller.dto.UserCredentials;
import org.example.user.controller.dto.FullName;
import org.example.user.controller.dto.UserProfile;
import org.example.trainee.controller.request.CreateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.controller.response.TraineeSummary;
import org.example.trainee.controller.request.UpdateTraineeRequest;
import org.example.trainer.controller.request.CreateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.controller.response.TrainerSummary;
import org.example.trainer.controller.request.UpdateTrainerRequest;
import org.example.training.controller.request.CreateTrainingRequest;
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
    public static final Long TRAINING_ID = 1L;

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
                TRAINER_USERNAME, TRAINEE_USERNAME, "Cardio",
                LocalDate.of(2026, 5, 12), 45
        );
    }

    public static UpdateTraineeRequest getUpdateTraineeRequest(){
        return new UpdateTraineeRequest(
                new FullName("John", "Doe"),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
    }

    public static UpdateTrainerRequest getUpdateTrainerRequest(){
        return new UpdateTrainerRequest(
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

    public static TraineeSummary getTraineeSummary(){
        return new TraineeSummary(
                TRAINEE_ID, new UserProfile(TRAINEE_USERNAME),
                LocalDate.of(2007, 3, 25), "Home 21 Street"
        );
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

    public static TraineeSummary getTraineeSummaryWithNoOptional(){
        return new TraineeSummary(
                TRAINEE_ID, new UserProfile(TRAINEE_USERNAME),
                null, null
        );
    }

    public static TrainerSummary getTrainerSummary(){
        return new TrainerSummary(
                TRAINER_ID, new UserProfile(TRAINER_USERNAME),
                TrainingType.YOGA
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

    public static TrainingSummary getTrainingSummary(){
        return new TrainingSummary(
                TRAINING_ID, getTrainerSummary(), getTraineeSummary(), "Morning Cardio",
                TrainingType.YOGA, LocalDate.of(2026, 5, 15), 60
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

    public static final String DEFAULT_PASSWORD = "password1234";
    public static final String DEFAULT_ADDRESS = "21 Home Street";
    public static final LocalDate DEFAULT_DATE_OF_BIRTH = LocalDate.of(2007, 1, 1);

    public static UserEntity buildUser(String username, String lastName) {
        UserEntity user = new UserEntity();
        user.setFirstName(FIRST_NAME);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(DEFAULT_PASSWORD);
        user.setIsActive(true);
        return user;
    }

    public static UserEntity buildUser(String username) {
        return buildUser(username, LAST_NAME);
    }

    public static TraineeEntity buildTrainee(UserEntity user) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(user);
        trainee.setAddress(DEFAULT_ADDRESS);
        trainee.setDateOfBirth(DEFAULT_DATE_OF_BIRTH);
        return trainee;
    }

    public static TrainingTypeEntity buildTrainingType(TrainingType name) {
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(name);
        return trainingType;
    }

    public static TrainerEntity buildTrainer(UserEntity user, TrainingTypeEntity specialization) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        return trainer;
    }

    public static TrainingEntity buildTraining(TraineeEntity trainee, TrainerEntity trainer, TrainingTypeEntity trainingType,
                                                String name, LocalDate date, int durationMinutes) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainingName(name);
        training.setTrainingDate(date);
        training.setDurationMinutes(durationMinutes);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        return training;
    }
}
