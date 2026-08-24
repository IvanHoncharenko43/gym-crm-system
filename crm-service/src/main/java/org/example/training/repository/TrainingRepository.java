package org.example.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long>, CustomTrainingRepository {

    @Query("""
            SELECT t FROM TrainingEntity t
            JOIN FETCH t.trainee tr JOIN FETCH tr.user
            JOIN FETCH t.trainer tn JOIN FETCH tn.user
            WHERE t.id = :id""")
    Optional<TrainingEntity> findById(Long id);

    @Query("""
            SELECT t FROM TrainingEntity t
            JOIN FETCH t.trainee tr JOIN FETCH tr.user tru
            JOIN FETCH t.trainer tn JOIN FETCH tn.user
            WHERE tru.username = :username""")
    List<TrainingEntity> findAllByTraineeUserUsername(String username);

    @Query("SELECT COUNT(t)>0 FROM TrainingEntity t WHERE t.id = :id AND t.trainer.user.username = :username")
    boolean existsByIdAndTrainerUserUsername(Long id, String username);
}
