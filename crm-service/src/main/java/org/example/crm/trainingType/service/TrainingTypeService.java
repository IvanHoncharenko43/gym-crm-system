package org.example.crm.trainingType.service;

import lombok.RequiredArgsConstructor;
import org.example.crm.trainingType.repository.TrainingTypeEntity;
import org.example.crm.trainingType.repository.TrainingTypeRepository;
import org.example.crm.trainingType.dto.TrainingTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    @Transactional(readOnly = true)
    public TrainingTypes getAllTrainingTypes(){
        return new TrainingTypes(trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::getTrainingTypeName)
                .toList()
        );
    }
}
