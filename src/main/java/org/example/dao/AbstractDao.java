package org.example.dao;


import lombok.extern.slf4j.Slf4j;
import org.example.domain.Identifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractDao<T extends Identifiable> {
    protected Map<Long, T> storage;
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

    public void initStorage(Map<Long, T> initialData) {
        if (this.storage == null) {
            this.storage = (initialData != null) ? new HashMap<>(initialData) : new HashMap<>();
            if (!this.storage.isEmpty()) {
                long maxId = this.storage.keySet().stream()
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0L);
                this.idCounter.set(maxId);
            }
        }
    }
}
