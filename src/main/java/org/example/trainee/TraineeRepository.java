package org.example.trainee;

import org.example.shared.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeRepository extends AbstractDao<Trainee> {

    public Optional<Trainee> findByUsername(String username){
        if(username == null){
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst();
    }
}
