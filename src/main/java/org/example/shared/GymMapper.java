package org.example.shared;

import org.example.trainee.*;
import org.example.trainer.*;
import org.example.training.CreateTrainingRequest;
import org.example.training.Training;
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

    public Trainee toTraineeEntity(CreateTraineeRequest request) {
        Objects.requireNonNull(request, "Create request cannot be null");
        Trainee trainee = new Trainee();
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

    public Trainee toTraineeEntity(UpdateTraineeRequest request, String username, String password) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Trainee trainee = new Trainee();
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

    public TraineeSummary toTraineeSummary(Trainee trainee) {
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

    public Trainer toTrainerEntity(CreateTrainerRequest request) {
        Objects.requireNonNull(request, "Create request cannot be null");
        Trainer trainer = new Trainer();
        String firstName = request.fullName().firstName();
        String lastName = request.fullName().lastName();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(usernameGenerator.generate(firstName, lastName));
        trainer.setPassword(passwordGenerator.generate());
        trainer.setSpecialization(request.specialization());
        return trainer;
    }

    public Trainer toTrainerEntity(UpdateTrainerRequest request, String username, String password) {
        Objects.requireNonNull(request, "Update request cannot be null");
        Trainer trainer = new Trainer();
        trainer.setId(request.id());
        trainer.setFirstName(request.fullName().firstName());
        trainer.setLastName(request.fullName().lastName());
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.isActive());
        return trainer;
    }

    public TrainerSummary toTrainerSummary(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerSummary(
                trainer.getId(),
                new UserProfile(
                        trainer.getUsername()
                ),
                trainer.getSpecialization()
        );
    }

    public Training toTraining(CreateTrainingRequest request, Trainee trainee, Trainer trainer) {
        Objects.requireNonNull(request, "Create request cannot be null");
        Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        Training training = new Training();
        training.setTraineeId(trainee.getId());
        training.setTrainerId(trainer.getId());
        training.setTrainingName(request.trainingName());
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(request.trainingDate());
        training.setDurationMinutes(request.durationMinutes());
        return training;
    }

    public TrainingSummary toTrainingSummary(Training training, Trainee trainee, Trainer trainer) {
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
