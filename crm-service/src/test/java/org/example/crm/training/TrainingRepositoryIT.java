package org.example.crm.training;

import org.example.crm.core.AbstractRepositoryIT;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.training.repository.TrainingRepository;
import org.example.crm.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.crm.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_PersistTrainingEntity_EntityIsNew() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));

        TrainingEntity training = buildTraining(
                trainee, trainer, trainingType, "Strength Basics", LocalDate.of(2024, 1, 10), 60);

        TrainingEntity saved = trainingRepository.save(training);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        TrainingEntity reloaded = entityManager.find(TrainingEntity.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getTrainingName()).isEqualTo("Strength Basics");
    }

    @Test
    void findById_ReturnTrainingWithTraineeAndTrainerUsers_IdExists() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.YOGA));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        TrainingEntity training = entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Yoga Session", LocalDate.of(2024, 2, 5), 45));
        entityManager.clear();

        Optional<TrainingEntity> existingTraining = trainingRepository.findById(training.getId());

        assertThat(existingTraining).isPresent();
        assertThat(existingTraining.get().getTrainee().getUser().getUsername()).isEqualTo("John.Doe");
        assertThat(existingTraining.get().getTrainer().getUser().getUsername()).isEqualTo("Jane.Smith");
    }

    @Test
    void findById_ReturnEmpty_IdDoesNotExist() {
        Optional<TrainingEntity> training = trainingRepository.findById(99L);

        assertThat(training).isEmpty();
    }

    @Test
    void findTraineeTrainings_ReturnFilteredTrainings_TraineeUsernameMatches() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60));

        UserEntity otherTraineeUser = entityManager.persistAndFlush(buildUser("Other.Trainee"));
        TraineeEntity otherTrainee = entityManager.persistAndFlush(buildTrainee(otherTraineeUser));
        entityManager.persistAndFlush(buildTraining(
                otherTrainee, trainer, trainingType, "Other Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTraineeTrainings(
                "John.Doe", null, null, null, null);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findTraineeTrainings_ReturnFilteredTrainings_TraineeUsernameAndOptionalFiltersMatch() {
        TrainingTypeEntity strength = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        TrainingTypeEntity cardio = entityManager.persistAndFlush(buildTrainingType(TrainingType.CARDIO));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity smithUser = entityManager.persistAndFlush(buildUser("Jane.Smith", "Smith"));
        TrainerEntity smithTrainer = entityManager.persistAndFlush(buildTrainer(smithUser, strength));
        UserEntity brownUser = entityManager.persistAndFlush(buildUser("Bob.Brown", "Brown"));
        TrainerEntity brownTrainer = entityManager.persistAndFlush(buildTrainer(brownUser, cardio));

        entityManager.persistAndFlush(buildTraining(
                trainee, smithTrainer, strength, "Matching Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.persistAndFlush(buildTraining(
                trainee, brownTrainer, cardio, "Non Matching Training", LocalDate.of(2024, 6, 1), 45));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTraineeTrainings(
                "John.Doe", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "Smith", TrainingType.STRENGTH.name());

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findTraineeTrainings_ReturnEmptyList_TraineeUsernameDoesNotMatch() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Some Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTraineeTrainings(
                "not.found", null, null, null, null);

        assertThat(trainings).isEmpty();
    }

    @Test
    void findTrainerTrainings_ReturnFilteredTrainings_TrainerUsernameMatches() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60));

        UserEntity otherTrainerUser = entityManager.persistAndFlush(buildUser("Other.Trainer"));
        TrainerEntity otherTrainer = entityManager.persistAndFlush(buildTrainer(otherTrainerUser, trainingType));
        entityManager.persistAndFlush(buildTraining(
                trainee, otherTrainer, trainingType, "Other Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTrainerTrainings(
                "Jane.Smith", null, null, null);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findTrainerTrainings_ReturnFilteredTrainings_TrainerUsernameAndOptionalFiltersMatch() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        UserEntity smithUser = entityManager.persistAndFlush(buildUser("John.Doe", "Smith"));
        TraineeEntity smithTrainee = entityManager.persistAndFlush(buildTrainee(smithUser));
        UserEntity brownUser = entityManager.persistAndFlush(buildUser("Other.Trainee", "Brown"));
        TraineeEntity brownTrainee = entityManager.persistAndFlush(buildTrainee(brownUser));

        entityManager.persistAndFlush(buildTraining(
                smithTrainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.persistAndFlush(buildTraining(
                brownTrainee, trainer, trainingType, "Non Matching Training", LocalDate.of(2024, 6, 1), 45));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTrainerTrainings(
                "Jane.Smith", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "Smith");

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findTrainerTrainings_ReturnEmptyList_TrainerUsernameDoesNotMatch() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Some Training", LocalDate.of(2024, 1, 10), 60));
        entityManager.clear();

        List<TrainingEntity> trainings = trainingRepository.findTrainerTrainings(
                "not.found", null, null, null);

        assertThat(trainings).isEmpty();
    }
}
