package org.example.training.repository;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
public class TrainingRepository {

    private final SessionFactory sessionFactory;

    public TrainingRepository(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public TrainingEntity save(TrainingEntity training) {
        Objects.requireNonNull(training, "Training cannot be null");
        Optional<TrainingEntity> existingTraining = findById(training.getId());
        if (existingTraining.isEmpty()) {
            getSession().persist(training);
            log.info("Created training with ID: {}", training.getId());
            return training;
        }
        TrainingEntity updatedTraining = getSession().merge(training);
        log.info("Updated training with ID: {}", updatedTraining.getId());
        return updatedTraining;
    }

    public Optional<TrainingEntity> findById(Long id){
        log.info("Started finding training by ID {}", id);
        String hql = "FROM TrainingEntity t JOIN FETCH t.trainee JOIN FETCH t.trainee.user JOIN FETCH t.trainer JOIN FETCH t.trainer.user WHERE t.id = :id";
        return getSession().createQuery(hql, TrainingEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public List<TrainingEntity> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName){
        Session session = getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> cq = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = cq.from(TrainingEntity.class);
        root.fetch("trainee", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainer", JoinType.LEFT).fetch("user", JoinType.LEFT);
        root.fetch("trainingType", JoinType.LEFT);

        List<Predicate> predicates = Stream.of(
                cb.equal(root.get("trainee").get("user").get("username"), traineeUsername),
                fromDate != null ? cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate) : null,
                toDate != null ? cb.lessThanOrEqualTo(root.get("trainingDate"), toDate) : null,
                trainerName != null ? cb.equal(root.get("trainer").get("user").get("lastName"), trainerName) : null,
                trainingTypeName != null ? cb.equal(root.get("trainingType").get("trainingTypeName"), trainingTypeName) : null
                )
                .filter(Objects::nonNull)
                .toList();

        cq.where(predicates.toArray(new Predicate[0]));
        log.info("Found trainee trainings by criteria");
        return session.createQuery(cq).getResultList();
    }

    public List<TrainingEntity> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName){
        Session session = getSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> cq = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = cq.from(TrainingEntity.class);
        root.fetch("trainer", JoinType.INNER).fetch("user", JoinType.INNER);
        root.fetch("trainee", JoinType.LEFT).fetch("user", JoinType.LEFT);
        root.fetch("trainingType", JoinType.LEFT);

        List<Predicate> predicates = Stream.of(
                cb.equal(root.get("trainer").get("user").get("username"), trainerUsername),
                fromDate != null ? cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate) : null,
                toDate != null ? cb.lessThanOrEqualTo(root.get("trainingDate"), toDate) : null,
                traineeName != null ? cb.equal(root.get("trainee").get("user").get("lastName"), traineeName) : null
                )
                .filter(Objects::nonNull)
                .toList();
        cq.where(predicates.toArray(new Predicate[0]));
        log.info("Found trainer trainings by criteria");
        return session.createQuery(cq).getResultList();
    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
