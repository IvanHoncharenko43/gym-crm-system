package org.example.trainer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

    @Query("SELECT t FROM TrainerEntity t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.id = :id")
    Optional<TrainerEntity> findById(Long id);

    @Query("SELECT t FROM TrainerEntity t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.user.username = :username")
    Optional<TrainerEntity> findByUsername(String username);

    @Query("SELECT t FROM TrainerEntity t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.user.username IN :usernames")
    List<TrainerEntity> findByUsernames(@Param("usernames") List<String> usernames);

    @Query("""
            SELECT tr FROM TrainerEntity tr
            JOIN FETCH tr.user
            JOIN FETCH tr.specialization
            WHERE NOT EXISTS (SELECT 1 FROM tr.trainees t WHERE t.user.username = :traineeUsername)""")
    List<TrainerEntity> findUnassignedTrainersByTraineeUsername(String traineeUsername);

    @Query("SELECT COUNT(t)>0 FROM TrainerEntity t WHERE t.user.id = :userId")
    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(t)>0 FROM TrainerEntity t WHERE t.id = :id AND t.user.username = :username")
    boolean existsByIdAndUserUsername(Long id, String username);
}
