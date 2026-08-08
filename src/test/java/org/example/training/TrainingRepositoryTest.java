package org.example.training;

import org.example.core.AbstractRepositoryTest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.training.repository.TrainingEntity;
import org.example.training.repository.TrainingRepository;
import org.example.training.repository.TrainingSpecifications;
import org.example.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username, String lastName) {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword("password1234");
        user.setIsActive(true);
        return entityManager.persistAndFlush(user);
    }

    private UserEntity persistUser(String username) {
        return persistUser(username, "Doe");
    }

    private TrainingTypeEntity persistTrainingType(TrainingType name) {
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(name);
        return entityManager.persistAndFlush(trainingType);
    }

    private TrainerEntity persistTrainer(String username, String lastName, TrainingTypeEntity specialization) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(persistUser(username, lastName));
        trainer.setSpecialization(specialization);
        return entityManager.persistAndFlush(trainer);
    }

    private TrainerEntity persistTrainer(String username, TrainingTypeEntity specialization) {
        return persistTrainer(username, "Doe", specialization);
    }

    private TraineeEntity persistTrainee(String username, String lastName) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(persistUser(username, lastName));
        trainee.setAddress("21 Home Street");
        trainee.setDateOfBirth(LocalDate.of(2007, 1, 1));
        return entityManager.persistAndFlush(trainee);
    }

    private TraineeEntity persistTrainee(String username) {
        return persistTrainee(username, "Doe");
    }

    private TrainingEntity persistTraining(TraineeEntity trainee, TrainerEntity trainer, TrainingTypeEntity trainingType,
                                            String name, LocalDate date, int durationMinutes) {
        TrainingEntity training = new TrainingEntity();
        training.setTrainingName(name);
        training.setTrainingDate(date);
        training.setDurationMinutes(durationMinutes);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        return entityManager.persistAndFlush(training);
    }

    @Test
    void save_PersistTrainingEntity_EntityIsNew() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TraineeEntity trainee = persistTrainee("John.Doe");
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);

        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Strength Basics");
        training.setTrainingDate(LocalDate.of(2024, 1, 10));
        training.setDurationMinutes(60);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

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
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.YOGA);
        TraineeEntity trainee = persistTrainee("John.Doe");
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        TrainingEntity training = persistTraining(trainee, trainer, trainingType, "Yoga Session", LocalDate.of(2024, 2, 5), 45);
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
    void findAll_ReturnFilteredTrainings_TraineeUsernameMatches() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TraineeEntity trainee = persistTrainee("John.Doe");
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        persistTraining(trainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60);

        TraineeEntity otherTrainee = persistTrainee("Other.Trainee");
        persistTraining(otherTrainee, trainer, trainingType, "Other Training", LocalDate.of(2024, 1, 10), 60);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTraineeTrainings(
                "John.Doe", null, null, null, null);
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findAll_ReturnFilteredTrainings_TraineeUsernameAndOptionalFiltersMatch() {
        TrainingTypeEntity strength = persistTrainingType(TrainingType.STRENGTH);
        TrainingTypeEntity cardio = persistTrainingType(TrainingType.CARDIO);
        TraineeEntity trainee = persistTrainee("John.Doe");
        TrainerEntity smithTrainer = persistTrainer("Jane.Smith", "Smith", strength);
        TrainerEntity brownTrainer = persistTrainer("Bob.Brown", "Brown", cardio);

        persistTraining(trainee, smithTrainer, strength, "Matching Training", LocalDate.of(2024, 1, 10), 60);
        persistTraining(trainee, brownTrainer, cardio, "Non Matching Training", LocalDate.of(2024, 6, 1), 45);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTraineeTrainings(
                "John.Doe", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "Smith", TrainingType.STRENGTH.name());
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findAll_ReturnEmptyList_TraineeUsernameDoesNotMatch() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TraineeEntity trainee = persistTrainee("John.Doe");
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        persistTraining(trainee, trainer, trainingType, "Some Training", LocalDate.of(2024, 1, 10), 60);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTraineeTrainings(
                "not.found", null, null, null, null);
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).isEmpty();
    }

    @Test
    void findAll_ReturnFilteredTrainings_TrainerUsernameMatches() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        TraineeEntity trainee = persistTrainee("John.Doe");
        persistTraining(trainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60);

        TrainerEntity otherTrainer = persistTrainer("Other.Trainer", trainingType);
        persistTraining(trainee, otherTrainer, trainingType, "Other Training", LocalDate.of(2024, 1, 10), 60);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTrainerTrainings(
                "Jane.Smith", null, null, null);
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findAll_ReturnFilteredTrainings_TrainerUsernameAndOptionalFiltersMatch() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        TraineeEntity smithTrainee = persistTrainee("John.Doe", "Smith");
        TraineeEntity brownTrainee = persistTrainee("Other.Trainee", "Brown");

        persistTraining(smithTrainee, trainer, trainingType, "Matching Training", LocalDate.of(2024, 1, 10), 60);
        persistTraining(brownTrainee, trainer, trainingType, "Non Matching Training", LocalDate.of(2024, 6, 1), 45);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTrainerTrainings(
                "Jane.Smith", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), "Smith");
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).extracting(TrainingEntity::getTrainingName)
                .containsExactly("Matching Training");
    }

    @Test
    void findAll_ReturnEmptyList_TrainerUsernameDoesNotMatch() {
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.STRENGTH);
        TrainerEntity trainer = persistTrainer("Jane.Smith", trainingType);
        TraineeEntity trainee = persistTrainee("John.Doe");
        persistTraining(trainee, trainer, trainingType, "Some Training", LocalDate.of(2024, 1, 10), 60);
        entityManager.clear();

        Specification<TrainingEntity> spec = TrainingSpecifications.findTrainerTrainings(
                "not.found", null, null, null);
        List<TrainingEntity> trainings = trainingRepository.findAll(spec);

        assertThat(trainings).isEmpty();
    }
}
