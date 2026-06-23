package org.example.dao;


import org.example.domain.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractDao<T extends Identifiable> {
    protected final Map<Long, T> storage;
    protected final AtomicLong idCounter;

    public AbstractDao(Map<Long, T> storage){
        this.storage = storage;
        this.idCounter = new AtomicLong(0);
    }

    public T create(T entity){
        Long id = idCounter.incrementAndGet();
        entity.setId(id);
        storage.put(id, entity);
        return entity;
    }

    public Optional<T> getById(Long id){
        return Optional.ofNullable(storage.get(id));
    }

    public List<T> getAll(){
        return new ArrayList<>(storage.values());
    }

    public T update(T entity){
        Long id = entity.getId();
        if (id == null || !storage.containsKey(id)) {
            throw new IllegalArgumentException("Entity with Id " + id + " does not exist. Cannot update");
        }
        storage.put(id, entity);
        return entity;
    }

    public void deleteById(Long id){
        storage.remove(id);
    }
}
