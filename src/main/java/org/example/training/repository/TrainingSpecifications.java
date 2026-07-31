package org.example.training.repository;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TrainingSpecifications{

    public static Specification<TrainingEntity> findTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
    ){
        return ((root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("trainee", JoinType.INNER).fetch("user", JoinType.INNER);
                root.fetch("trainer", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("trainingType", JoinType.LEFT);
            }
            List<Predicate> predicates = Stream.of(
                            cb.equal(root.get("trainee").get("user").get("username"), traineeUsername),
                            fromDate != null ? cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate) : null,
                            toDate != null ? cb.lessThanOrEqualTo(root.get("trainingDate"), toDate) : null,
                            trainerName != null ? cb.equal(root.get("trainer").get("user").get("lastName"), trainerName) : null,
                            trainingTypeName != null ? cb.equal(root.get("trainingType").get("trainingTypeName"), trainingTypeName) : null
                    )
                    .filter(Objects::nonNull)
                    .toList();
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<TrainingEntity> findTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ){
        return ((root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("trainer", JoinType.INNER).fetch("user", JoinType.INNER);
                root.fetch("trainee", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("trainingType", JoinType.LEFT);
            }
            List<Predicate> predicates = Stream.of(
                            cb.equal(root.get("trainer").get("user").get("username"), trainerUsername),
                            fromDate != null ? cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate) : null,
                            toDate != null ? cb.lessThanOrEqualTo(root.get("trainingDate"), toDate) : null,
                            traineeName != null ? cb.equal(root.get("trainee").get("user").get("lastName"), traineeName) : null
                    )
                    .filter(Objects::nonNull)
                    .toList();
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
