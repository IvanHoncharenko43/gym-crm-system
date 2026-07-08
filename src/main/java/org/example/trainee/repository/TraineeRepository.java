package org.example.trainee.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.core.repository.AbstractRepository;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class TraineeRepository extends AbstractRepository<TraineeEntity> {

    public TraineeRepository(SessionFactory sessionFactory){
        super(sessionFactory, TraineeEntity.class);
    }

    public Optional<TraineeEntity> findByUsername(String username){
        log.info("Started getting trainee by username");
        String query = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        return getSession().createQuery(query, TraineeEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void deleteByUsername(String username){
        findByUsername(username).ifPresent(trainee -> {
            getSession().remove(trainee);
            log.info("Trainee delete by username");
        });
    }
}
