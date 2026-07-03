package org.example.shared;

import org.example.trainee.*;
import org.example.trainer.*;
import org.example.training.CreateTrainingRequest;
import org.example.training.TrainingEntity;
import org.example.training.TrainingSummary;
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
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(usernameGenerator.generate(firstName, lastName));
        trainee.setPassword(passwordGenerator.generate());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
    }

    public TraineeEntity toTraineeEntity(UpdateTraineeRequest request, String username, String password) {
        Objects.requireNonNull(request, "Update request cannot be null");
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(request.id());
        trainee.setFirstName(request.fullName().firstName());
        trainee.setLastName(request.fullName().lastName());
        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.isActive());
        return trainee;
    }

    public TraineeSummary toTraineeSummary(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        return new TraineeSummary(
                trainee.getId(),
                new UserProfile(
                        trainee.getUsername()
                ),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public TrainerEntity toTrainerEntity(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Create request cannot be null");
        TrainerEntity trainer = new TrainerEntity();
        String firstName = request.fullName().firstName();
        String lastName = request.fullName().lastName();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(usernameGenerator.generate(firstName, lastName));
        trainer.setPassword(passwordGenerator.generate());
        trainer.setSpecialization(request.specialization());
        return trainer;
    }

    public TrainerEntity toTrainerEntity(UpdateTrainerRequest request, String username, String password) {
        Objects.requireNonNull(request, "Update request cannot be null");
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(request.id());
        trainer.setFirstName(request.fullName().firstName());
        trainer.setLastName(request.fullName().lastName());
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.isActive());
        return trainer;
    }

    public TrainerSummary toTrainerSummary(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerSummary(
                trainer.getId(),
                new UserProfile(
                        trainer.getUsername()
                ),
                trainer.getSpecialization()
        );
    }

    public TrainingEntity toTraining(CreateTrainingRequest request, TraineeEntity trainee, TrainerEntity trainer) {
        Objects.requireNonNull(request, "Create request cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        TrainingEntity training = new TrainingEntity();
        training.setTraineeId(trainee.getId());
        training.setTrainerId(trainer.getId());
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
                training.getTrainingType(),
                training.getTrainingDate(),
                training.getDurationMinutes()
        );
    }
}
