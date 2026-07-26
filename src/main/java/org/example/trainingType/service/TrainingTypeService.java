package org.example.trainingType.service;

import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.trainingType.dto.TrainingTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository){
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional(readOnly = true)
    public TrainingTypes getAllTrainingTypes(){
        return new TrainingTypes(trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::getTrainingTypeName)
                .toList()
        );
    }
}
