package org.example.macrocycle.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MacrocycleJobRepository extends MongoRepository<MacrocycleJobDocument, String> {
}
