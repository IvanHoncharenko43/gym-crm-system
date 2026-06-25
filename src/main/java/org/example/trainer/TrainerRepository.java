package org.example.trainer;

import org.example.shared.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerRepository extends AbstractDao<Trainer> {

    public Optional<Trainer> findByUsername(String username){
        if(username == null){
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(trainer -> username.equals(trainer.getUsername()))
                .findFirst();
    }

}
