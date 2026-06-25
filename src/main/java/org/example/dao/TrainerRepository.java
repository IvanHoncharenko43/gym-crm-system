package org.example.dao;

import org.example.trainer.Trainer;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerRepository extends AbstractDao<Trainer> {

    public TrainerRepository(Map<Long, Trainer> storage){
        super(storage);
    }

    public Optional<Trainer> findByUsername(String username){
        if(username == null){
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }

}
