package org.example.crm.core.service;

import lombok.RequiredArgsConstructor;
import org.example.crm.core.dto.ActionType;
import org.example.crm.trainee.controller.request.CreateTraineeRequest;
import org.example.crm.trainee.controller.response.TraineeSummary;
import org.example.crm.trainee.controller.request.UpdateTraineeRequest;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainer.controller.request.CreateTrainerRequest;
import org.example.crm.trainer.controller.request.TrainerWorkloadRequest;
import org.example.crm.trainer.controller.response.TrainerSummary;
import org.example.crm.trainer.controller.request.UpdateTrainerRequest;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.training.controller.request.CreateTrainingRequest;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.controller.response.TrainingSummary;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.user.controller.dto.FullName;
import org.example.crm.user.controller.dto.UserProfile;
import org.example.crm.user.repository.UserEntity;
import org.example.crm.utils.PasswordGenerator;
import org.example.crm.utils.UsernameGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GymMapper {

    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    public TraineeEntity toTraineeEntity(CreateTraineeRequest request, Set<String> existingUsernames) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(toNewUser(request.fullName(), existingUsernames));
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
    }

    public TraineeEntity toTraineeEntity(UpdateTraineeRequest request, TraineeEntity trainee) {
        UserEntity updatedUser = toUpdatedUser(trainee.getUser(), request.fullName());
        TraineeEntity updatedTrainee = new TraineeEntity();
        updatedTrainee.setId(trainee.getId());
        updatedTrainee.setUser(updatedUser);
        updatedTrainee.setDateOfBirth(request.dateOfBirth());
        updatedTrainee.setAddress(request.address());
        updatedTrainee.setTrainers(trainee.getTrainers());
        updatedTrainee.setTrainings(trainee.getTrainings());
        return updatedTrainee;
    }

    public TraineeSummary toTraineeSummary(TraineeEntity trainee) {
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
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(toNewUser(request.fullName(), existingUsernames));
        trainer.setSpecialization(trainingType);
        return trainer;
    }

    public TrainerEntity toTrainerEntity(UpdateTrainerRequest request, TrainerEntity trainer, TrainingTypeEntity trainingType) {
        UserEntity updatedUser = toUpdatedUser(trainer.getUser(), request.fullName());
        TrainerEntity updatedTrainer = new TrainerEntity();
        updatedTrainer.setId(trainer.getId());
        updatedTrainer.setUser(updatedUser);
        updatedTrainer.setSpecialization(trainingType);
        updatedTrainer.setTrainees(trainer.getTrainees());
        return updatedTrainer;
    }

    public TrainerSummary toTrainerSummary(TrainerEntity trainer) {
        return new TrainerSummary(
                trainer.getId(),
                new UserProfile(
                        trainer.getUser().getUsername()
                ),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public TrainerWorkloadRequest toTrainerWorkloadRequest(TrainerEntity trainer, TrainingEntity training, ActionType actionType){
        return new TrainerWorkloadRequest(
                trainer.getUser().getUsername(),
                new FullName(trainer.getUser().getFirstName(), trainer.getUser().getLastName()),
                trainer.getUser().getIsActive(),
                training.getTrainingDate(),
                training.getDurationMinutes(),
                actionType
        );
    }

    public TrainingEntity toTraining(CreateTrainingRequest request, TraineeEntity trainee, TrainerEntity trainer) {
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

    private UserEntity toNewUser(FullName fullName, Set<String> existingUsernames){
        String firstName = fullName.firstName();
        String lastName = fullName.lastName();
        UserEntity newUser = new UserEntity();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(usernameGenerator.generate(firstName, lastName, existingUsernames));
        newUser.setPassword(passwordEncoder.encode(passwordGenerator.generate()));
        newUser.setIsActive(true);
        return newUser;
    }

    private UserEntity toUpdatedUser(UserEntity user, FullName fullName){
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
