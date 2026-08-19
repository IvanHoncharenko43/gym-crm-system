package org.example.training.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Repository
public class CustomTrainingRepositoryImpl implements CustomTrainingRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TrainingEntity> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = query.from(TrainingEntity.class);

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
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<TrainingEntity> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> root = query.from(TrainingEntity.class);

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
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}
