package org.example.trainer;

import org.example.shared.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerRepository extends AbstractRepository<Trainer> {

    public Optional<Trainer> findByUsername(String username){
        if(username == null){
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }
}
