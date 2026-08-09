package org.example.trainingType.service;

import org.example.core.service.AuthenticationComponent;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.trainingType.dto.TrainingTypes;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationComponent authenticator;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository, AuthenticationComponent authenticator){
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticator = authenticator;
    }

    @Transactional(readOnly = true)
    public TrainingTypes getAllTrainingTypes(UserCredentials credentials){
        authenticator.authenticate(credentials);
        return new TrainingTypes(trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::getTrainingTypeName)
                .toList()
        );
    }
}
