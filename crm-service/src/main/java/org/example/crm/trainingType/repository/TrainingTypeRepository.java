package org.example.crm.trainingType.repository;

import org.example.crm.trainingType.dto.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, Long> {

    Optional<TrainingTypeEntity> findByTrainingTypeName(TrainingType trainingTypeName);
}
