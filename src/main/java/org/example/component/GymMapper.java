package org.example.component;

import org.example.shared.UserProfile;
import org.example.trainee.*;
import org.example.trainer.*;
import org.example.training.CreateTrainingRequest;
import org.example.training.Training;
import org.example.training.TrainingResponse;
import org.springframework.stereotype.Component;

@Component
public class GymMapper {

    public Trainee toTrainee(CreateTraineeRequest request) {
        java.util.Objects.requireNonNull(request, "Create request cannot be null");
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        return trainee;
    }

    public Trainee toTrainee(UpdateTraineeRequest request) {
        java.util.Objects.requireNonNull(request, "Update request cannot be null");
        Trainee trainee = new Trainee();
        trainee.setId(request.id());
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.isActive());
        return trainee;
    }

    public TraineeResponse toTraineeResponse(Trainee trainee) {
        java.util.Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        return new TraineeResponse(
                trainee.getId(),
                new UserProfile(
                        trainee.getFirstName(),
                        trainee.getLastName(),
                        trainee.getUsername(),
                        trainee.getPassword(),
                        trainee.isActive()
                ),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public TraineeSummary toTraineeSummary(Trainee trainee) {
        java.util.Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        return new TraineeSummary(
                trainee.getId(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getUsername(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
    }

    public Trainer toTrainer(CreateTrainerRequest request) {
        java.util.Objects.requireNonNull(request, "Create request cannot be null");
        Trainer trainer = new Trainer();
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        return trainer;
    }

    public Trainer toTrainer(UpdateTrainerRequest request) {
        java.util.Objects.requireNonNull(request, "Update request cannot be null");
        Trainer trainer = new Trainer();
        trainer.setId(request.id());
        trainer.setFirstName(request.firstName());
        trainer.setLastName(request.lastName());
        trainer.setSpecialization(request.specialization());
        trainer.setActive(request.isActive());
        return trainer;
    }

    public TrainerResponse toTrainerResponse(Trainer trainer) {
        java.util.Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerResponse(
                trainer.getId(),
                new UserProfile(
                        trainer.getFirstName(),
                        trainer.getLastName(),
                        trainer.getUsername(),
                        trainer.getPassword(),
                        trainer.isActive()
                ),
                trainer.getSpecialization()
        );
    }

    public TrainerSummary toTrainerSummary(Trainer trainer) {
        java.util.Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainerSummary(
                trainer.getId(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getUsername(),
                trainer.getSpecialization()
        );
    }

    public Training toTraining(CreateTrainingRequest request, Trainee trainee, Trainer trainer) {
        java.util.Objects.requireNonNull(request, "Create request cannot be null");
        Training training = new Training();
        training.setTraineeId(trainee.getId());
        training.setTrainerId(trainer.getId());
        training.setTrainingName(request.trainingName());
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(request.trainingDate());
        training.setDuration(request.duration());
        return training;
    }

    public TrainingResponse toTrainingResponse(Training training, Trainee trainee, Trainer trainer) {
        java.util.Objects.requireNonNull(training, "Training entity cannot be null");
        java.util.Objects.requireNonNull(trainee, "Trainee entity cannot be null");
        java.util.Objects.requireNonNull(trainer, "Trainer entity cannot be null");
        return new TrainingResponse(
                training.getId(),
                toTrainerSummary(trainer),
                toTraineeSummary(trainee),
                training.getTrainingName(),
                training.getTrainingType(),
                training.getTrainingDate(),
                training.getDuration()
        );
    }
}
