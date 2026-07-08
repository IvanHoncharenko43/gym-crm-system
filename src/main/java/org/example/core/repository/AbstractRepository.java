package org.example.core.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;
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
                .createQuery("from " + entityClass.getSimpleName(), entityClass)
                .getResultList();
    }

    public T update(T entity){
        Objects.requireNonNull(entity, "Entity cannot be null");
        Long id = entity.getId();
        if (getSession().find(entityClass, id) == null) {
            log.error("Entity with ID {} does not exist", id);
            throw new NotFoundException("Entity with Id " + id + " does not exist");
        }
        getSession().merge(entity);
        log.info("Updated entity with ID: {}", id);
        return entity;
    }

    public void deleteById(Long id){
        T reference = getSession().getReference(entityClass, id);
        getSession().remove(reference);
        log.info("Deleted entity with ID: {}", id);
    }

    protected Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
