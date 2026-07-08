package org.example.trainer.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.core.repository.AbstractRepository;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        // Шукаємо всіх тренерів, для яких НЕ ІСНУЄ запису в колекції trainees з таким username
        String hql = "SELECT tr FROM TrainerEntity tr " +
                "JOIN FETCH tr.user " + // Уникаємо N+1 для юзера тренера
                "JOIN FETCH tr.specialization " + // Уникаємо N+1 для спеціалізації
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM tr.trainees t WHERE t.user.username = :username" +
                ")";

        return getSession().createQuery(hql, TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    public void deleteByUsername(String username){
        findByUsername(username).ifPresent(trainer -> {
            getSession().remove(trainer);
            log.info("Trainer delete by username");
        });
    }
}
