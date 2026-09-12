package org.example.workload.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.example.workload.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TrainerWorkloadRepositoryIT {

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void cleanUp() {
        mongoTemplate.dropCollection(TrainerWorkloadDocument.class);
    }

    @Test
    void save_PersistTrainerWorkloadEntity_EntityIsNew() {
        TrainerWorkloadDocument trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);

        TrainerWorkloadDocument savedWorkload = trainerWorkloadRepository.save(trainerWorkload);

        assertThat(savedWorkload.getId()).isNotNull();
        TrainerWorkloadDocument existingWorkload = mongoTemplate.findById(savedWorkload.getId(), TrainerWorkloadDocument.class);
        assertThat(existingWorkload).isNotNull();
        assertThat(existingWorkload.getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(existingWorkload.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(existingWorkload.getLastName()).isEqualTo(LAST_NAME);
        assertThat(existingWorkload.isStatus()).isTrue();
    }

    @Test
    void save_PersistTrainerWorkloadWithYearsAndMonths_EntityHasYearsAndMonths() {
        TrainerWorkloadDocument trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        trainerWorkload.getMonths().add(monthWorkload);

        TrainerWorkloadDocument savedWorkload = trainerWorkloadRepository.save(trainerWorkload);

        TrainerWorkloadDocument existingWorkload = mongoTemplate.findById(savedWorkload.getId(), TrainerWorkloadDocument.class);
        assertThat(existingWorkload).isNotNull();
        assertThat(existingWorkload.getYear()).isEqualTo(TRAINING_DATE.getYear());
        assertThat(existingWorkload.getMonths()).hasSize(1);
        MonthWorkload existingMonth = existingWorkload.getMonths().iterator().next();
        assertThat(existingMonth.getMonth()).isEqualTo(TRAINING_DATE.getMonth());
        assertThat(existingMonth.getTrainingSummaryDurationMinutes()).isEqualTo(DURATION_MINUTES);
    }

    @Test
    void findByUsernameAndYear_ReturnEntityWithYearsAndMonths_UsernameExists() {
        TrainerWorkloadDocument trainerWorkload = getTrainerWorkloadEntity(TRAINER_USERNAME, FIRST_NAME, LAST_NAME, true);
        MonthWorkload monthWorkload = getMonthWorkloadEntity(TRAINING_DATE.getMonth(), DURATION_MINUTES);
        trainerWorkload.getMonths().add(monthWorkload);
        mongoTemplate.save(trainerWorkload);

        Optional<TrainerWorkloadDocument> result = trainerWorkloadRepository.findByUsernameAndYear(trainerWorkload.getUsername(), trainerWorkload.getYear());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(TRAINER_USERNAME);
        assertThat(result.get().getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.get().getLastName()).isEqualTo(LAST_NAME);
        assertThat(result.get().getYear()).isEqualTo(TRAINING_DATE.getYear());
        assertThat(result.get().isStatus()).isTrue();
        assertThat(result.get().getMonths()).hasSize(1);
    }

    @Test
    void findByUsernameAndYear_ReturnEmpty_UsernameDoesNotExist() {
        Optional<TrainerWorkloadDocument> result = trainerWorkloadRepository.findByUsernameAndYear("not.found", 2026);
        assertThat(result).isEmpty();
    }
}
