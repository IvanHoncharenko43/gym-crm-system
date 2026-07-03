package org.example.trainee;

import org.example.shared.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeRepository extends AbstractRepository<Trainee> {

    public Optional<Trainee> findByUsername(String username){
        return storage.values().stream()
                .filter(trainee -> username.equals(trainee.getUsername()))
                .findFirst();
    }
}
