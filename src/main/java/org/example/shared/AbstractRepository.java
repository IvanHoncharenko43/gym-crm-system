package org.example.shared;


import lombok.extern.slf4j.Slf4j;
import org.example.exception.NotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractRepository<T extends Identifiable> {
    protected Map<Long, T> storage;
    protected AtomicLong idCounter = new AtomicLong(0);;

    public T create(T entity){
        Objects.requireNonNull(entity, "Entity cannot be null");
        Long id = idCounter.incrementAndGet();
        entity.setId(id);
        storage.put(id, entity);
        log.info("Created entity with ID: {}", id);
        return entity;
    }

    public Optional<T> getById(Long id){
        return Optional.ofNullable(storage.get(id));
    }

    public List<T> getAll(){
        return List.copyOf(storage.values());
    }

    public T update(T entity){
        Objects.requireNonNull(entity, "Entity cannot be null");
        Long id = entity.getId();
        if (!storage.containsKey(id)) {
            log.error("Entity with ID {} does not exist", id);
            throw new NotFoundException("Entity with Id " + id + " does not exist");
        }
        storage.put(id, entity);
        log.info("Updated entity with ID: {}", id);
        return entity;
    }

    public void deleteById(Long id){
        storage.remove(id);
        log.info("Deleted entity with ID: {}", id);
    }

    public void initStorage(Map<Long, T> initialData) {
        if (this.storage == null) {
            this.storage = (initialData != null) ? new ConcurrentHashMap<>(initialData) : new ConcurrentHashMap<>();
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
