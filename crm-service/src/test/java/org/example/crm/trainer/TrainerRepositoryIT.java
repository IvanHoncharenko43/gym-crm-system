package org.example.crm.trainer;

import org.example.crm.core.AbstractRepositoryIT;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.trainer.repository.TrainerRepository;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.example.crm.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainerRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_PersistTrainerEntity_EntityIsNew() {
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        TrainerEntity trainer = buildTrainer(user, specialization);

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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        TrainingTypeEntity newSpecialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.CARDIO));
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(user, specialization));
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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.YOGA));
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(user, specialization));
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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.FLEXIBILITY));
        UserEntity user = entityManager.persistAndFlush(buildUser(username));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(user, specialization));
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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity janeUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        entityManager.persistAndFlush(buildTrainer(janeUser, specialization));
        UserEntity johnUser = entityManager.persistAndFlush(buildUser("John.Doe"));
        entityManager.persistAndFlush(buildTrainer(johnUser, specialization));
        UserEntity excludedUser = entityManager.persistAndFlush(buildUser("Not.Included"));
        entityManager.persistAndFlush(buildTrainer(excludedUser, specialization));
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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser(traineeUsername));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity assignedTrainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity assignedTrainer = entityManager.persistAndFlush(buildTrainer(assignedTrainerUser, specialization));
        UserEntity unassignedTrainerUser = entityManager.persistAndFlush(buildUser("Unassigned.Doe"));
        entityManager.persistAndFlush(buildTrainer(unassignedTrainerUser, specialization));

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
        TrainingTypeEntity specialization = entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        UserEntity traineeUser = entityManager.persistAndFlush(buildUser(traineeUsername));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(traineeUser));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, specialization));

        trainee.getTrainers().add(trainer);
        entityManager.flush();
        entityManager.clear();

        List<TrainerEntity> unassignedTrainers = trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);

        assertThat(unassignedTrainers).isEmpty();
    }
}
