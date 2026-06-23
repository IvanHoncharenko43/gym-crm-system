package org.example.dao;

import org.example.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDao extends AbstractDao<Trainer> {

    public TrainerDao(Map<Long, Trainer> storage){
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
