package org.example.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long>, JpaSpecificationExecutor<TrainingEntity> {

    @Query("""
            SELECT t FROM TrainingEntity t
            JOIN FETCH t.trainee tr JOIN FETCH tr.user
            JOIN FETCH t.trainer tn JOIN FETCH tn.user
            WHERE t.id = :id""")
    Optional<TrainingEntity> findById(@Param("id") Long id);
}
