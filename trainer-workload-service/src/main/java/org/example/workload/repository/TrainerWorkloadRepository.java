package org.example.workload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkloadEntity, Long> {
    @Query("""
           SELECT t FROM TrainerWorkloadEntity t
           LEFT JOIN FETCH t.years y
           LEFT JOIN FETCH y.months
           WHERE t.username = :username
           """)
    Optional<TrainerWorkloadEntity> findByUsername(String username);
}
