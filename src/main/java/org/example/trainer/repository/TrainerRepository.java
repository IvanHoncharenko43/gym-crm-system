package org.example.trainer.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.core.repository.AbstractRepository;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class TrainerRepository extends AbstractRepository<TrainerEntity> {

    public TrainerRepository(SessionFactory sessionFactory){
        super(sessionFactory, TrainerEntity.class);
    }

    public Optional<TrainerEntity> findByUsername(String username){
        log.info("Started getting trainer by username");
        String query = "FROM TrainerEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        return getSession().createQuery(query, TrainerEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void deleteByUsername(String username){
        findByUsername(username).ifPresent(trainer -> {
            getSession().remove(trainer);
            log.info("Trainer delete by username");
        });
    }
}
