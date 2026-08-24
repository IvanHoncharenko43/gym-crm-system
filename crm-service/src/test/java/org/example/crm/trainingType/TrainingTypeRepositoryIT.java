package org.example.crm.trainingType;

import org.example.crm.core.AbstractRepositoryIT;
import org.example.crm.trainingType.dto.TrainingType;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.trainingType.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.example.crm.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TrainingTypeRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAll_ReturnAllTrainingTypes_DataExists() {
        entityManager.persistAndFlush(buildTrainingType(TrainingType.STRENGTH));
        entityManager.persistAndFlush(buildTrainingType(TrainingType.YOGA));
        entityManager.clear();

        List<TrainingTypeEntity> found = trainingTypeRepository.findAll();

        assertThat(found).hasSize(2)
                .extracting(TrainingTypeEntity::getTrainingTypeName)
                .containsExactlyInAnyOrder(TrainingType.STRENGTH, TrainingType.YOGA);
    }

    @Test
    void findAll_ReturnEmptyList_NoDataExists() {
        List<TrainingTypeEntity> found = trainingTypeRepository.findAll();

        assertThat(found).isEmpty();
    }

    @Test
    void findByTrainingTypeName_ReturnTrainingType_NameExists() {
        TrainingTypeEntity trainingType = entityManager.persistAndFlush(buildTrainingType(TrainingType.CARDIO));
        entityManager.clear();

        Optional<TrainingTypeEntity> found = trainingTypeRepository.findByTrainingTypeName(TrainingType.CARDIO);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(trainingType.getId());
    }

    @Test
    void findByTrainingTypeName_ReturnEmpty_NameDoesNotExist() {
        Optional<TrainingTypeEntity> found = trainingTypeRepository.findByTrainingTypeName(TrainingType.FLEXIBILITY);

        assertThat(found).isEmpty();
    }
}
