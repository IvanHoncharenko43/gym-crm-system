package org.example.workload.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadEntity, String> {

    Optional<TrainerWorkloadEntity> findByUsernameAndYear(String username, int year);
}