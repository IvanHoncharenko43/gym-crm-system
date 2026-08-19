package org.example.trainingType.repository;

import org.example.trainingType.dto.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, Long> {

    Optional<TrainingTypeEntity> findByTrainingTypeName(TrainingType trainingTypeName);
}
