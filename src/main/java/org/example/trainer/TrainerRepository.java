package org.example.trainer;

import org.example.shared.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerRepository extends AbstractRepository<TrainerEntity> {

    public Optional<TrainerEntity> findByUsername(String username){
        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }
}
