package org.example.dao;

import org.example.training.Training;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingRepository extends AbstractDao<Training> {

    public TrainingRepository(Map<Long, Training> storage){
        super(storage);
    }
}
