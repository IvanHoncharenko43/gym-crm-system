package org.example.trainee;

import org.example.core.AbstractRepositoryTest;
import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.trainer.repository.TrainerEntity;
import org.example.training.repository.TrainingEntity;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.user.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TraineeRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

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

    private TraineeEntity persistTrainee(String username) {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(persistUser(username));
        trainee.setAddress("21 Home Street");
        trainee.setDateOfBirth(LocalDate.of(2007, 1, 1));
        return entityManager.persistAndFlush(trainee);
    }

    @Test
    void save_PersistTraineeEntity_EntityIsNew() {
        UserEntity user = persistUser("John.Doe");
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(user);
        trainee.setAddress("21 Home Street");
        trainee.setDateOfBirth(LocalDate.of(2007, 1, 1));

        TraineeEntity saved = traineeRepository.save(trainee);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        TraineeEntity reloaded = entityManager.find(TraineeEntity.class, saved.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getAddress()).isEqualTo("21 Home Street");
    }

    @Test
    void save_MergeTraineeEntity_EntityHasId() {
        TraineeEntity trainee = persistTrainee("John.Doe");
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
        TraineeEntity trainee = persistTrainee(username);
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
        TraineeEntity trainee = persistTrainee(username);
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
        TraineeEntity trainee = persistTrainee(username);
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
        TraineeEntity trainee = persistTrainee(username);

        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(TrainingType.CARDIO);
        entityManager.persistAndFlush(trainingType);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(persistUser("Jane.Smith"));
        trainer.setSpecialization(trainingType);
        entityManager.persistAndFlush(trainer);

        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Cascade Test Training");
        training.setTrainingDate(LocalDate.now());
        training.setDurationMinutes(60);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        entityManager.persistAndFlush(training);
        Long trainingId = training.getId();

        entityManager.clear();

        traineeRepository.deleteByUserUsername(username);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(TrainingEntity.class, trainingId)).isNull();
    }

    @Test
    void deleteByUserUsername_DoNothing_UsernameDoesNotExist() {
        persistTrainee("John.Doe");
        long countBefore = traineeRepository.count();

        traineeRepository.deleteByUserUsername("not.real");
        entityManager.flush();

        assertThat(traineeRepository.count()).isEqualTo(countBefore);
    }
}
