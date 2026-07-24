package org.example.core.repository;

import org.example.training.dto.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingTypeRepository {
    private final SessionFactory sessionFactory;

    public TrainingTypeRepository(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public Optional<TrainingTypeEntity> findByName(TrainingType typeName) {
        String hql = "FROM TrainingTypeEntity t WHERE t.trainingTypeName = :typeName";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, TrainingTypeEntity.class)
                .setParameter("typeName", typeName)
                .uniqueResultOptional();
    }
}
