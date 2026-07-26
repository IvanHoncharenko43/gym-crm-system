package org.example.training.service;

import org.example.core.repository.TrainingTypeEntity;
import org.example.core.repository.TrainingTypeRepository;
import org.example.training.dto.request.TrainingTypes;
import org.springframework.stereotype.Service;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository){
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public TrainingTypes getAllTrainingTypes(){
        return new TrainingTypes(trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::getTrainingTypeName)
                .toList()
        );
    }
}
