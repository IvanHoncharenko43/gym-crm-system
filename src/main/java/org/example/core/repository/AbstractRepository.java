package org.example.core.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRepository<T extends Identifiable> {
    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;

    public T create(T entity){
        Objects.requireNonNull(entity, "Entity cannot be null");
        getSession().persist(entity);
        log.info("Created entity with ID: {}", entity.getId());
        return entity;
    }

    public Optional<T> getById(Long id){
        return Optional.ofNullable(getSession().find(entityClass, id));
    }

    public List<T> getAll(){
        return getSession()
                .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .getResultList();
    }

    public T update(T entity){
        Objects.requireNonNull(entity, "Entity cannot be null");
        Long id = entity.getId();
        getSession().merge(entity);
        log.info("Updated entity with ID: {}", id);
        return entity;
    }

    public void deleteById(Long id){
        T entity = getSession().find(entityClass, id);
        if(entity != null) {
            getSession().remove(entity);
            log.info("Deleted entity with ID: {}", id);
        }
    }

    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
