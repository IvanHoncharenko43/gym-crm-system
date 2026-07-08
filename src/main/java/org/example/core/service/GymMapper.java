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
import org.example.training.dto.TrainingTypeSummary;
import org.example.training.repository.TrainingEntity;
import org.example.training.dto.TrainingSummary;
import org.example.training.repository.TrainingTypeEntity;
import org.example.user.dto.UserProfile;
import org.example.utils.PasswordGenerator;
import org.example.utils.UsernameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GymMapper {

    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator){
        this.passwordGenerator = passwordGenerator;
    }

    public TraineeEntity toTraineeEntity(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Create request cannot be null");
        TraineeEntity trainee = new TraineeEntity();
        String firstName = request.fullName().firstName();
        String lastName = request.fullName().lastName();
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.getUser().setUsername(usernameGenerator.generate(firstName, lastName));
        trainee.getUser().setPassword(passwordGenerator.generate());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.getUser().setIsActive(true);
        return trainee;
    }

    public TraineeEntity toTraineeEntity(UpdateTraineeRequest request, TraineeEntity trainee) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        String requestedFirstName = request.fullName().firstName();
        String requestedLastName = request.fullName().lastName();

        trainee.setId(request.id());
        trainee.getUser().setFirstName(request.fullName().firstName());
        trainee.getUser().setLastName(request.fullName().lastName());
        if(!requestedFirstName.equals(trainee.getUser().getFirstName()) ||
                !requestedLastName.equals(trainee.getUser().getLastName())){
            usernameGenerator.removeUsername(trainee.getUser().getUsername());
            trainee.getUser().setUsername(usernameGenerator.generate(requestedFirstName, requestedLastName));
        }
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
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

    public TrainerEntity toTrainerEntity(CreateTrainerRequest request, TrainingTypeEntity trainingType) {
        Objects.requireNonNull(request, "Create request cannot be null");
        TrainerEntity trainer = new TrainerEntity();
        String firstName = request.fullName().firstName();
        String lastName = request.fullName().lastName();
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.getUser().setUsername(usernameGenerator.generate(firstName, lastName));
        trainer.getUser().setPassword(passwordGenerator.generate());
        trainer.setSpecialization(trainingType);
        trainer.getUser().setIsActive(true);
        return trainer;
    }

    public TrainerEntity toTrainerEntity(UpdateTrainerRequest request, TrainerEntity trainer, TrainingTypeEntity trainingType) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        String requestedFirstName = request.fullName().firstName();
        String requestedLastName = request.fullName().lastName();

        trainer.setId(request.id());
        trainer.getUser().setFirstName(request.fullName().firstName());
        trainer.getUser().setLastName(request.fullName().lastName());
        if(!requestedFirstName.equals(trainer.getUser().getFirstName()) ||
                !requestedLastName.equals(trainer.getUser().getLastName())){
            usernameGenerator.removeUsername(trainer.getUser().getUsername());
            trainer.getUser().setUsername(usernameGenerator.generate(requestedFirstName, requestedLastName));
        }
        trainer.setSpecialization(trainingType);
        return trainer;
    }

    public TrainerSummary toTrainerSummary(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerSummary(
                trainer.getId(),
                new UserProfile(
                        trainer.getUser().getUsername()
                ),
                toTrainingTypeSummary(trainer.getSpecialization())
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
                toTrainingTypeSummary(trainer.getSpecialization()),
                training.getTrainingDate(),
                training.getDurationMinutes()
        );
    }

    public TrainingTypeSummary toTrainingTypeSummary(TrainingTypeEntity trainingType){
        return new TrainingTypeSummary(
                trainingType.getId(),
                trainingType.getTrainingTypeName()
        );
    }
}
