package org.example.trainingType;

import org.example.core.AbstractRepositoryTest;
import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingTypeRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TrainingTypeEntity persistTrainingType(TrainingType name) {
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setTrainingTypeName(name);
        return entityManager.persistAndFlush(trainingType);
    }

    @Test
    void findAll_ReturnAllTrainingTypes_DataExists() {
        persistTrainingType(TrainingType.STRENGTH);
        persistTrainingType(TrainingType.YOGA);
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
        TrainingTypeEntity trainingType = persistTrainingType(TrainingType.CARDIO);
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
