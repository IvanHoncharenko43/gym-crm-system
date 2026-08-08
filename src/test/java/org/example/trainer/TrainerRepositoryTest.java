package org.example.trainer;

import org.example.core.AbstractRepositoryTest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username) {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword("password1234");
        user.setIsActive(true);
        return entityManager.persistAndFlush(user);
    }

    private TrainingTypeEntity persistTrainingType(TrainingType name) {
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(name);
        return entityManager.persistAndFlush(trainingType);
    }

    private TrainerEntity persistTrainer(String username, TrainingTypeEntity specialization) {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(persistUser(username));
        trainer.setSpecialization(specialization);
        return entityManager.persistAndFlush(trainer);
    }

    private TraineeEntity persistTrainee(String username) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(persistUser(username));
        trainee.setAddress("21 Home Street");
        trainee.setDateOfBirth(LocalDate.of(2007, 1, 1));
        return entityManager.persistAndFlush(trainee);
    }

    @Test
    void save_PersistTrainerEntity_EntityIsNew() {
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.STRENGTH);
        UserEntity user = persistUser("John.Doe");
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        TrainerEntity saved = trainerRepository.save(trainer);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        TrainerEntity reloaded = entityManager.find(TrainerEntity.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getUser().getUsername()).isEqualTo("John.Doe");
    }

    @Test
    void save_MergeTrainerEntity_EntityHasId() {
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.STRENGTH);
        TrainingTypeEntity newSpecialization = persistTrainingType(TrainingType.CARDIO);
        TrainerEntity trainer = persistTrainer("John.Doe", specialization);
        entityManager.clear();

        TrainerEntity trainerToUpdate = trainerRepository.findById(trainer.getId()).orElseThrow();
        trainerToUpdate.setSpecialization(newSpecialization);
        trainerRepository.save(trainerToUpdate);
        entityManager.flush();
        entityManager.clear();

        TrainerEntity reloaded = entityManager.find(TrainerEntity.class, trainer.getId());
        assertThat(reloaded.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingType.CARDIO);
    }

    @Test
    void findById_ReturnTrainerWithUserAndSpecialization_IdExists() {
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.YOGA);
        TrainerEntity trainer = persistTrainer("John.Doe", specialization);
        entityManager.clear();

        Optional<TrainerEntity> existingTrainer = trainerRepository.findById(trainer.getId());

        assertThat(existingTrainer).isPresent();
        assertThat(existingTrainer.get().getUser().getUsername()).isEqualTo("John.Doe");
        assertThat(existingTrainer.get().getSpecialization().getTrainingTypeName()).isEqualTo(TrainingType.YOGA);
    }

    @Test
    void findById_ReturnEmpty_IdDoesNotExist() {
        Optional<TrainerEntity> trainer = trainerRepository.findById(99L);

        assertThat(trainer).isEmpty();
    }

    @Test
    void findByUsername_ReturnTrainerWithUserAndSpecialization_UsernameExists() {
        String username = "John.Doe";
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.FLEXIBILITY);
        TrainerEntity trainer = persistTrainer(username, specialization);
        entityManager.clear();

        Optional<TrainerEntity> existingTrainer = trainerRepository.findByUsername(username);

        assertThat(existingTrainer).isPresent();
        assertThat(existingTrainer.get().getId()).isEqualTo(trainer.getId());
        assertThat(existingTrainer.get().getUser().getUsername()).isEqualTo(username);
    }

    @Test
    void findByUsername_ReturnEmpty_UsernameDoesNotExist() {
        Optional<TrainerEntity> trainer = trainerRepository.findByUsername("not.found");

        assertThat(trainer).isEmpty();
    }

    @Test
    void findByUsernames_ReturnTrainersList_UsernamesExist() {
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.STRENGTH);
        persistTrainer("Jane.Smith", specialization);
        persistTrainer("John.Doe", specialization);
        persistTrainer("Not.Included", specialization);
        entityManager.clear();

        List<TrainerEntity> trainers = trainerRepository.findByUsernames(List.of("Jane.Smith", "John.Doe"));

        assertThat(trainers).extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("Jane.Smith", "John.Doe");
    }

    @Test
    void findByUsernames_ReturnEmptyList_UsernamesDoNotExist() {
        List<TrainerEntity> trainers = trainerRepository.findByUsernames(List.of("not.found"));

        assertThat(trainers).isEmpty();
    }

    @Test
    void findUnassignedTrainersByTraineeUsername_ReturnTrainersList_UnassignedTrainersExist() {
        String traineeUsername = "John.Doe";
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.STRENGTH);
        TraineeEntity trainee = persistTrainee(traineeUsername);
        TrainerEntity assignedTrainer = persistTrainer("Jane.Smith", specialization);
        persistTrainer("Unassigned.Doe", specialization);

        trainee.getTrainers().add(assignedTrainer);
        entityManager.flush();
        entityManager.clear();

        List<TrainerEntity> unassignedTrainers = trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);

        assertThat(unassignedTrainers).extracting(t -> t.getUser().getUsername())
                .containsExactly("Unassigned.Doe");
    }

    @Test
    void findUnassignedTrainersByTraineeUsername_ReturnEmptyList_AllTrainersAssigned() {
        String traineeUsername = "John.Doe";
        TrainingTypeEntity specialization = persistTrainingType(TrainingType.STRENGTH);
        TraineeEntity trainee = persistTrainee(traineeUsername);
        TrainerEntity trainer = persistTrainer("Jane.Smith", specialization);

        trainee.getTrainers().add(trainer);
        entityManager.flush();
        entityManager.clear();

        List<TrainerEntity> unassignedTrainers = trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);

        assertThat(unassignedTrainers).isEmpty();
    }
}
