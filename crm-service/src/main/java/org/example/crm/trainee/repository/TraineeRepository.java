package org.example.crm.trainee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {

    @Query("SELECT t FROM TraineeEntity t JOIN FETCH t.user WHERE t.id = :id")
    Optional<TraineeEntity> findById(Long id);

    @Query("SELECT t FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username")
    Optional<TraineeEntity> findByUsername(String username);

    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.username = :username")
    void deleteByUserUsername(String username);

    @Query("SELECT COUNT(t)>0 FROM TraineeEntity t WHERE t.user.id = :userId")
    boolean existsByUserId(Long userId);

    @Query("SELECT COUNT(t)>0 FROM TraineeEntity t WHERE t.id = :id AND t.user.username = :username")
    boolean existsByIdAndUserUsername(Long id, String username);
}
