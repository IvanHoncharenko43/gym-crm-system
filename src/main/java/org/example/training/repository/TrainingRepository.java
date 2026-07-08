package org.example.training.repository;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.example.core.repository.AbstractRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TrainingRepository extends AbstractRepository<TrainingEntity> {
    public TrainingRepository(SessionFactory sessionFactory){
        super(sessionFactory, TrainingEntity.class);
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

        List<Predicate> predicates = setDatePredicates(fromDate, toDate, cb, root);
        predicates.add(cb.equal(root.get("trainee").get("user").get("username"), traineeUsername));
        if (trainerName != null) {
            predicates.add(cb.equal(root.get("trainer").get("user").get("lastName"), trainerName));
        }
        if (trainingTypeName != null) {
            predicates.add(cb.equal(root.get("trainingType").get("trainingTypeName"), trainingTypeName));
        }
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

        List<Predicate> predicates = setDatePredicates(fromDate, toDate, cb, root);
        predicates.add(cb.equal(root.get("trainer").get("user").get("username"), trainerUsername));
        if (traineeName != null) {
            predicates.add(cb.equal(root.get("trainee").get("user").get("lastName"), traineeName));
        }
        cq.where(predicates.toArray(new Predicate[0]));
        log.info("Found trainer trainings by criteria");
        return session.createQuery(cq).getResultList();
    }

    private List<Predicate> setDatePredicates(LocalDate fromDate, LocalDate toDate, CriteriaBuilder cb, Root<TrainingEntity> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
        }
        return predicates;
    }
}
