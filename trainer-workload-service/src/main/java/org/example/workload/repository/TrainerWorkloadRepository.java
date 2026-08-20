package org.example.workload.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TrainerWorkloadRepository {
    private final Map<String, TrainerWorkloadEntity> repository = new ConcurrentHashMap<>();

    public TrainerWorkloadEntity save(TrainerWorkloadEntity trainerWorkloadEntity){
        return repository.put(trainerWorkloadEntity.getUsername(), trainerWorkloadEntity);
    }

    public Optional<TrainerWorkloadEntity> findByUsername(String username){
        return Optional.ofNullable(repository.get(username));
    }

    public void deleteByUsername(String username){
        repository.remove(username);
    }
}
