package org.example.trainee.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class TraineeRepository {
    private final SessionFactory sessionFactory;

    public TraineeRepository(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public TraineeEntity save(TraineeEntity trainee){
        Objects.requireNonNull(trainee, "Trainee cannot be null");
        if (trainee.getId() == null) {
            getSession().persist(trainee);
            log.info("Created trainee with ID: {}", trainee.getId());
            return trainee;
        } else {
            TraineeEntity updatedTrainee = getSession().merge(trainee);
            log.info("Updated trainee with ID: {}", updatedTrainee.getId());
            return updatedTrainee;
        }
    }

    public Optional<TraineeEntity> findByUsername(String username){
        log.info("Started getting trainee by username");
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        return getSession().createQuery(hql, TraineeEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public Optional<TraineeEntity> findById(Long id){
        log.info("Started finding trainee by ID {}", id);
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.id = :id";
        return getSession().createQuery(hql, TraineeEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public void deleteByUsername(String username){
        findByUsername(username).ifPresent(trainee -> {
            getSession().remove(trainee);
            log.info("Trainee deleted by username");
        });
    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
