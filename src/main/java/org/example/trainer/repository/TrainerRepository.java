package org.example.trainer.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
public class TrainerRepository {

    private final SessionFactory sessionFactory;

    public TrainerRepository(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public TrainerEntity save(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, "Trainer cannot be null");
        Optional<TrainerEntity> existingTrainer = findById(trainer.getId());
        if (existingTrainer.isEmpty()) {
            getSession().persist(trainer);
            log.info("Created trainer with ID: {}", trainer.getId());
            return trainer;
        }
        TrainerEntity updatedTrainer = getSession().merge(trainer);
        log.info("Updated trainer with ID: {}", updatedTrainer.getId());
        return updatedTrainer;
    }

    public Optional<TrainerEntity> findByUsername(String username){
        log.info("Started getting trainer by username");
        String hql = "FROM TrainerEntity t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.user.username = :username";
        return getSession().createQuery(hql, TrainerEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public Optional<TrainerEntity> findById(Long id){
        log.info("Started finding trainer by ID {}", id);
        String hql = "FROM TrainerEntity t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.id = :id";
        return getSession().createQuery(hql, TrainerEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public List<TrainerEntity> findByUsernames(List<String> usernames){
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        String hql = "SELECT tr FROM TrainerEntity tr " +
                "JOIN FETCH tr.user " +
                "JOIN FETCH tr.specialization " +
                "WHERE tr.user.username IN :usernames";
        return getSession().createQuery(hql, TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }

    public List<TrainerEntity> findUnassignedTrainersByTraineeUsername(String traineeUsername) {
        String hql = """ 
                SELECT tr FROM TrainerEntity tr 
                JOIN FETCH tr.user
                JOIN FETCH tr.specialization
                WHERE NOT EXISTS (SELECT 1 FROM tr.trainees t WHERE t.user.username = :username)""";
        return getSession().createQuery(hql, TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
