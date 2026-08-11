package org.example.trainingType.service;

import lombok.RequiredArgsConstructor;
import org.example.core.service.AuthenticationComponent;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.trainingType.dto.TrainingTypes;
import org.example.user.controller.dto.UserCredentials;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationComponent authenticator;

    @Transactional(readOnly = true)
    public TrainingTypes getAllTrainingTypes(UserCredentials credentials){
        authenticator.authenticate(credentials);
        return new TrainingTypes(trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::getTrainingTypeName)
                .toList()
        );
    }
}
