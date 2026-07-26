package org.example.core.service;

import org.example.trainee.dto.CreateTraineeRequest;
import org.example.trainee.dto.TraineeSummary;
import org.example.trainee.dto.UpdateTraineeRequest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.dto.CreateTrainerRequest;
import org.example.trainer.dto.TrainerSummary;
import org.example.trainer.dto.UpdateTrainerRequest;
import org.example.trainer.repository.TrainerEntity;
import org.example.training.dto.CreateTrainingRequest;
import org.example.training.repository.TrainingEntity;
import org.example.training.dto.TrainingSummary;
import org.example.core.repository.TrainingTypeEntity;
import org.example.user.dto.FullName;
import org.example.user.dto.UserProfile;
import org.example.user.repository.UserEntity;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class GymMapper {

    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public GymMapper(UsernameGenerator usernameGenerator, PasswordGenerator passwordGenerator){
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public TraineeEntity toTraineeEntity(CreateTraineeRequest request, Set<String> existingUsernames) {
        Objects.requireNonNull(request, "Create request cannot be null");
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(toNewUser(request.fullName(), existingUsernames));
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
    }

    public TraineeEntity toTraineeEntity(UpdateTraineeRequest request, TraineeEntity trainee) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        UserEntity updatedUser = toUpdatedUser(trainee.getUser(), request.fullName());
        TraineeEntity updatedTrainee = new TraineeEntity();
        updatedTrainee.setId(trainee.getId());
        updatedTrainee.setUser(updatedUser);
        updatedTrainee.setDateOfBirth(request.dateOfBirth());
        updatedTrainee.setAddress(request.address());
        return updatedTrainee;
    }

    public TraineeSummary toTraineeSummary(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        return new TraineeSummary(
                trainee.getId(),
                new UserProfile(
                        trainee.getUser().getUsername()
                ),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public TrainerEntity toTrainerEntity(CreateTrainerRequest request, TrainingTypeEntity trainingType, Set<String> existingUsernames) {
        Objects.requireNonNull(request, "Create request cannot be null");
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(toNewUser(request.fullName(), existingUsernames));
        trainer.setSpecialization(trainingType);
        return trainer;
    }

    public TrainerEntity toTrainerEntity(UpdateTrainerRequest request, TrainerEntity trainer, TrainingTypeEntity trainingType) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        UserEntity updatedUser = toUpdatedUser(trainer.getUser(), request.fullName());
        TrainerEntity updatedTrainer = new TrainerEntity();
        updatedTrainer.setId(trainer.getId());
        updatedTrainer.setUser(updatedUser);
        updatedTrainer.setSpecialization(trainingType);
        return trainer;
    }

    public TrainerSummary toTrainerSummary(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerSummary(
                trainer.getId(),
                new UserProfile(
                        trainer.getUser().getUsername()
                ),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public TrainingEntity toTraining(CreateTrainingRequest request, TraineeEntity trainee, TrainerEntity trainer) {
        Objects.requireNonNull(request, "Create request cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        TrainingEntity training = new TrainingEntity();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.trainingName());
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(request.trainingDate());
        training.setDurationMinutes(request.durationMinutes());
        return training;
    }

    public TrainingSummary toTrainingSummary(TrainingEntity training, TraineeEntity trainee, TrainerEntity trainer) {
        Objects.requireNonNull(training, "Training entity cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainingSummary(
                training.getId(),
                toTrainerSummary(trainer),
                toTraineeSummary(trainee),
                training.getTrainingName(),
                trainer.getSpecialization().getTrainingTypeName(),
                training.getTrainingDate(),
                training.getDurationMinutes()
        );
    }

    public UserProfile toUserProfile(UserEntity user){
        Objects.requireNonNull(user, "User entity cannot be null");
        return new UserProfile(user.getUsername());
    }

    private UserEntity toNewUser(FullName fullName, Set<String> existingUsernames){
        Objects.requireNonNull(fullName, "Full name cannot be null");
        String firstName = fullName.firstName();
        String lastName = fullName.lastName();
        UserEntity newUser = new UserEntity();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(usernameGenerator.generate(firstName, lastName, existingUsernames));
        newUser.setPassword(passwordGenerator.generate());
        newUser.setIsActive(true);
        return newUser;
    }

    private UserEntity toUpdatedUser(UserEntity user, FullName fullName){
        Objects.requireNonNull(user, "User entity cannot be null");
        Objects.requireNonNull(fullName, "Full name cannot be null");
        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(user.getId());
        updatedUser.setFirstName(fullName.firstName());
        updatedUser.setLastName(fullName.lastName());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setIsActive(user.getIsActive());
        return updatedUser;
    }
}
