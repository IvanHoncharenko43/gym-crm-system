package org.example.trainee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {

    @Query("SELECT t FROM TraineeEntity t JOIN FETCH t.user WHERE t.id = :id")
    Optional<TraineeEntity> findById(@Param("id") Long id);

    @Query("SELECT t FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username")
    Optional<TraineeEntity> findByUsername(@Param("username") String username);

    void deleteByUserUsername(String username);
}
