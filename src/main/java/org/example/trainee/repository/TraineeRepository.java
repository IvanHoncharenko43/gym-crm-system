package org.example.trainee.repository;

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
}
