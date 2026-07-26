package org.example.core.repository;

import org.example.training.dto.TrainingType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeRepository {
    private final SessionFactory sessionFactory;

    public TrainingTypeRepository(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public Optional<TrainingTypeEntity> findByName(TrainingType typeName) {
        String hql = "FROM TrainingTypeEntity t WHERE t.trainingTypeName = :typeName";
        return getSession().createQuery(hql, TrainingTypeEntity.class)
                .setParameter("typeName", typeName)
                .uniqueResultOptional();
    }

    public List<TrainingTypeEntity> findAll(){
        String hql = "FROM TrainingTypeEntity";
        return getSession().createQuery(hql, TrainingTypeEntity.class)
                .getResultList();
    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
