package org.example.dao;

import org.example.domain.Training;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingDao extends AbstractDao<Training> {

    public TrainingDao(Map<Long, Training> storage){
        super(storage);
    }
}
