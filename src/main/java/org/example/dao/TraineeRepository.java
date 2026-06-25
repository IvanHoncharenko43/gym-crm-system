package org.example.dao;

import org.example.trainee.Trainee;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeRepository extends AbstractDao<Trainee> {

    public TraineeRepository(Map<Long, Trainee> storage){
        super(storage);
    }

    public Optional<Trainee> findByUsername(String username){
        if(username == null){
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst();
    }
}
