package org.example.workload.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Repository
public class TrainerWorkloadRepository {
    private final Map<String, TrainerWorkloadEntity> repository = new ConcurrentHashMap<>();

    public TrainerWorkloadEntity computeAndSave(String username, UnaryOperator<TrainerWorkloadEntity> updater){
        return repository.compute(username, (key, existing) -> updater.apply(existing));
    }

    public Optional<TrainerWorkloadEntity> findByUsername(String username){
        return Optional.ofNullable(repository.get(username));
    }
}
