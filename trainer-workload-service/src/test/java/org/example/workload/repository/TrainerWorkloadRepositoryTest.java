package org.example.workload.repository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.example.workload.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadRepositoryTest {

    private final TrainerWorkloadRepository trainerWorkloadRepository = new TrainerWorkloadRepository();

    @Test
    void computeAndSave_CreateNewEntity_UsernameDoesNotExist() {
        TrainerWorkloadEntity createdWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        TrainerWorkloadEntity result = trainerWorkloadRepository.computeAndSave(TRAINER_USERNAME, created -> createdWorkload);
        assertThat(result).isSameAs(createdWorkload);
        Optional<TrainerWorkloadEntity> stored = trainerWorkloadRepository.findByUsername(TRAINER_USERNAME);
        assertThat(stored).isPresent();
        assertThat(stored.get()).isSameAs(createdWorkload);
    }

    @Test
    void computeAndSave_PassExistingEntityToUpdater_UsernameExists() {
        TrainerWorkloadEntity existingWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        trainerWorkloadRepository.computeAndSave(TRAINER_USERNAME, existing -> existingWorkload);

        TrainerWorkloadEntity result = trainerWorkloadRepository.computeAndSave(TRAINER_USERNAME, existing -> existingWorkload);

        assertThat(result).isSameAs(existingWorkload);
        Optional<TrainerWorkloadEntity> stored = trainerWorkloadRepository.findByUsername(TRAINER_USERNAME);
        assertThat(stored).isPresent();
        assertThat(stored.get()).isSameAs(existingWorkload);
    }

    @Test
    void findByUsername_ReturnEntity_UsernameExists() {
        TrainerWorkloadEntity entity = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        trainerWorkloadRepository.computeAndSave(TRAINER_USERNAME, ignored -> entity);

        Optional<TrainerWorkloadEntity> result = trainerWorkloadRepository.findByUsername(TRAINER_USERNAME);
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(result.get().getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.get().getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    void findByUsername_ReturnEmpty_UsernameDoesNotExist() {
        Optional<TrainerWorkloadEntity> result = trainerWorkloadRepository.findByUsername("not.found");
        assertThat(result).isEmpty();
    }
}
