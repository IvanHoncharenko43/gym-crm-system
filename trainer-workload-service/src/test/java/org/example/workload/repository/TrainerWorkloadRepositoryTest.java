package org.example.workload.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.example.workload.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainerWorkloadRepositoryTest {

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_PersistTrainerWorkloadEntity_EntityIsNew() {
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        TrainerWorkloadEntity savedWorkload = trainerWorkloadRepository.save(trainerWorkload);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedWorkload.getId()).isNotNull();
        TrainerWorkloadEntity existingWorkload = entityManager.find(TrainerWorkloadEntity.class, savedWorkload.getId());
        assertThat(existingWorkload).isNotNull();
        assertThat(existingWorkload.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(existingWorkload.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(existingWorkload.getLastName()).isEqualTo(LAST_NAME);
        assertThat(existingWorkload.isStatus()).isTrue();
    }

    @Test
    void save_PersistTrainerWorkloadWithYearsAndMonths_EntityHasYearsAndMonths() {
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        trainerWorkload.getYears().add(yearWorkload);
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        yearWorkload.getMonths().add(monthWorkload);

        TrainerWorkloadEntity savedWorkload = trainerWorkloadRepository.save(trainerWorkload);
        entityManager.flush();
        entityManager.clear();

        TrainerWorkloadEntity existingWorkload = entityManager.find(TrainerWorkloadEntity.class, savedWorkload.getId());
        assertThat(existingWorkload.getYears()).hasSize(1);
        YearWorkloadEntity existingYear = existingWorkload.getYears().iterator().next();
        assertThat(existingYear.getYear()).isEqualTo(TRAINING_DATE.getYear());
        assertThat(existingYear.getMonths()).hasSize(1);
        MonthWorkloadEntity existingMonth = existingYear.getMonths().iterator().next();
        assertThat(existingMonth.getMonth()).isEqualTo(TRAINING_DATE.getMonth());
        assertThat(existingMonth.getTrainingSummaryDurationMinutes()).isEqualTo(DURATION_MINUTES);
    }

    @Test
    void findByUsername_ReturnEntityWithYearsAndMonths_UsernameExists() {
        TrainerWorkloadEntity trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        YearWorkloadEntity yearWorkload = getYearWorkloadEntity(TRAINING_DATE.getYear());
        trainerWorkload.getYears().add(yearWorkload);
        MonthWorkloadEntity monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        yearWorkload.getMonths().add(monthWorkload);
        entityManager.persistAndFlush(trainerWorkload);
        entityManager.clear();

        Optional<TrainerWorkloadEntity> result = trainerWorkloadRepository.findByUsername(TRAINER_USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(result.get().getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.get().getLastName()).isEqualTo(LAST_NAME);
        assertThat(result.get().getYears()).hasSize(1);
        assertThat(result.get().getYears().iterator().next().getMonths()).hasSize(1);
    }

    @Test
    void findByUsername_ReturnEmpty_UsernameDoesNotExist() {
        Optional<TrainerWorkloadEntity> result = trainerWorkloadRepository.findByUsername("not.found");
        assertThat(result).isEmpty();
    }
}
