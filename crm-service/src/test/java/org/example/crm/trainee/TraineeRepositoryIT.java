package org.example.crm.trainee;

import org.example.crm.core.AbstractRepositoryIT;
import org.example.crm.trainee.repository.TraineeEntity;
import org.example.crm.trainee.repository.TraineeRepository;
import org.example.crm.trainer.repository.TrainerEntity;
import org.example.crm.training.repository.TrainingEntity;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.example.crm.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TraineeRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_PersistTraineeEntity_EntityIsNew() {
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = buildTrainee(user);

        TraineeEntity saved = traineeRepository.save(trainee);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        TraineeEntity reloaded = entityManager.find(TraineeEntity.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getAddress()).isEqualTo(DEFAULT_ADDRESS);
    }

    @Test
    void save_MergeTraineeEntity_EntityHasId() {
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(user));
        entityManager.clear();

        TraineeEntity traineeToUpdate = traineeRepository.findById(trainee.getId()).orElseThrow();
        traineeToUpdate.setAddress("Updated Address");
        traineeRepository.save(traineeToUpdate);
        entityManager.flush();
        entityManager.clear();

        TraineeEntity reloaded = entityManager.find(TraineeEntity.class, trainee.getId());
        assertThat(reloaded.getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void findById_ReturnTraineeWithUser_IdExists() {
        String username = "John.Doe";
        UserEntity user = entityManager.persistAndFlush(buildUser(username));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(user));
        entityManager.clear();

        Optional<TraineeEntity> existingUser = traineeRepository.findById(trainee.getId());

        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getUser().getUsername()).isEqualTo(username);
    }

    @Test
    void findById_ReturnEmpty_IdDoesNotExist() {
        Optional<TraineeEntity> user = traineeRepository.findById(99L);

        assertThat(user).isEmpty();
    }

    @Test
    void findByUsername_ReturnTraineeWithUser_UsernameExists() {
        String username = "John.Doe";
        UserEntity user = entityManager.persistAndFlush(buildUser(username));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(user));
        entityManager.clear();

        Optional<TraineeEntity> existingUser = traineeRepository.findByUsername(username);

        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getId()).isEqualTo(trainee.getId());
        assertThat(existingUser.get().getUser().getUsername()).isEqualTo(username);
    }

    @Test
    void findByUsername_ReturnEmpty_UsernameDoesNotExist() {
        Optional<TraineeEntity> user = traineeRepository.findByUsername("not.found");

        assertThat(user).isEmpty();
    }

    @Test
    void deleteByUserUsername_RemoveTraineeAndCascadeUser_UsernameExists() {
        String username = "John.Doe";
        UserEntity user = entityManager.persistAndFlush(buildUser(username));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(user));
        Long traineeId = trainee.getId();
        Long userId = trainee.getUser().getId();
        entityManager.clear();

        traineeRepository.deleteByUserUsername(username);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(TraineeEntity.class, traineeId)).isNull();
        assertThat(entityManager.find(UserEntity.class, userId)).isNull();
    }

    @Test
    void deleteByUserUsername_CascadeRemoveTrainings_UsernameExists() {
        String username = "John.Doe";
        UserEntity user = entityManager.persistAndFlush(buildUser(username));
        TraineeEntity trainee = entityManager.persistAndFlush(buildTrainee(user));

        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.CARDIO));
        UserEntity trainerUser = entityManager.persistAndFlush(buildUser("Jane.Smith"));
        TrainerEntity trainer = entityManager.persistAndFlush(buildTrainer(trainerUser, trainingType));
        TrainingEntity training = entityManager.persistAndFlush(buildTraining(
                trainee, trainer, trainingType, "Cascade Test Training", LocalDate.now(), 60));
        Long trainingId = training.getId();

        entityManager.clear();

        traineeRepository.deleteByUserUsername(username);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(TrainingEntity.class, trainingId)).isNull();
    }

    @Test
    void deleteByUserUsername_DoNothing_UsernameDoesNotExist() {
        UserEntity user = entityManager.persistAndFlush(buildUser("John.Doe"));
        entityManager.persistAndFlush(buildTrainee(user));
        long countBefore = traineeRepository.count();

        traineeRepository.deleteByUserUsername("not.real");
        entityManager.flush();

        assertThat(traineeRepository.count()).isEqualTo(countBefore);
    }
}
